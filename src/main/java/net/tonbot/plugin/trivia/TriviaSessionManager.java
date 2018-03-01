package net.tonbot.plugin.trivia;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.TriviaTopic;

class TriviaSessionManager {

	private final TriviaLibrary triviaLibrary;
	private final DifficultyBasedQuestionSelector questionSelector;
	private final Random random;
	private final QuestionHandlers questionHandlers;
	
	private final Map<TriviaSessionKey, TriviaSession> sessions;
	private final ReadWriteLock lock;

	@Inject
	public TriviaSessionManager(
			TriviaLibrary triviaLibrary, 
			DifficultyBasedQuestionSelector questionSelector,
			Random random, 
			QuestionHandlers questionHandlers) {
		this.triviaLibrary = Preconditions.checkNotNull(triviaLibrary, "triviaLibrary must be non-null.");
		this.questionSelector = Preconditions.checkNotNull(questionSelector, "questionSelector must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
		this.questionHandlers = Preconditions.checkNotNull(questionHandlers, "questionHandlers must be non-null.");

		this.sessions = new HashMap<>();
		this.lock = new ReentrantReadWriteLock();
	}

	public Optional<TriviaSession> getSession(TriviaSessionKey sessionKey) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		
		lock.readLock().lock();
		try {
			return Optional.ofNullable(this.sessions.get(sessionKey));
		} finally {
			lock.readLock().unlock();
		}
		
	}
	
	/**
	 * Attempts to create a session, if there is currently no ongoing session for the given key.
	 * @param sessionKey
	 *            {@link TriviaSessionKey}. Non-null.
	 * @param triviaTopicName
	 *            The trivia topic name. Must be the name of a valid trivia topic.
	 *            Non-null.
	 * @param difficulty
	 *            The {@link Difficulty.} Non-null.
	 * @return The new {@link TriviaSession}.
	 * @throws InvalidTopicException
	 *            if the specified trivia topic is not valid.
	 * @throws ExistingSessionException
	 * 			 if the session already exists.
	 */
	public TriviaSession tryCreateSession(
			TriviaSessionKey sessionKey, 
			String triviaTopicName, 
			Difficulty difficulty,
			TriviaListener listener) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		Preconditions.checkNotNull(triviaTopicName, "triviaTopicName must be non-null.");
		Preconditions.checkNotNull(difficulty, "difficulty must be non-null.");
		
		lock.writeLock().lock();
		try {
			TriviaSession currentSession = this.sessions.get(sessionKey);
			
			if (currentSession != null) {
				throw new ExistingSessionException("A session already exists for key " + sessionKey);
			}
			
			LoadedTrivia loadedTrivia = triviaLibrary.getTrivia(triviaTopicName)
					.orElseThrow(() -> new InvalidTopicException("Trivia topic " + triviaTopicName + " is not valid."));
			TriviaConfiguration triviaConfig = getConfigFor(loadedTrivia.getTriviaTopic(), difficulty);
			
			TriviaSession triviaSession = new TriviaSession(this, listener, loadedTrivia, triviaConfig, questionSelector, random, questionHandlers);
			this.sessions.put(sessionKey, triviaSession);

			triviaSession.start();
			
			return triviaSession;
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * Stops the corresponding session nicely and removes it from this manager.
	 * No-op if there is no session associated with the given key.
	 * 
	 * @param sessionKey Session Key. Non-null.
	 * @return The stopped {@link TriviaSession}, if one existed for the given key.
	 */
	public Optional<TriviaSession> stopSession(TriviaSessionKey sessionKey) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		
		lock.writeLock().lock();
		try {
			TriviaSession session = sessions.remove(sessionKey);
			
			if (session != null) {
				session.end();
			}
			
			return Optional.ofNullable(session);
		} finally {
			lock.writeLock().unlock();
		}
	}
	
	/**
	 * A {@link TriviaSession} is expected to call this method when it is done so that it can be removed from the manager.
	 * @param session The session that has ended.
	 */
	void sessionHasEnded(TriviaSession session) {
		lock.writeLock().lock();
		try {
			Optional<Entry<TriviaSessionKey, TriviaSession>> entry = this.sessions.entrySet().stream()
				.filter(e -> e.getValue() == session)
				.findAny();
			
			if (entry.isPresent()) {
				this.sessions.remove(entry.get().getKey());
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	private TriviaConfiguration getConfigFor(TriviaTopic topic, Difficulty difficulty) {
		Preconditions.checkNotNull(topic, "topic must be non-null.");
		Preconditions.checkNotNull(difficulty, "difficulty must be non-null.");
		
		int maxQuestions = topic.getMetadata().getDefaultQuestionsPerRound();
		long defaultTimePerQuestion = topic.getMetadata().getDefaultTimePerQuestion();
		
		TriviaConfiguration tc = TriviaConfiguration.builder()
				.maxQuestions(maxQuestions)
				.defaultTimePerQuestion(defaultTimePerQuestion)
				.difficulty(difficulty)
				.scoreDecayFactor(difficulty.getScoreDecayFactor())
				.maxMultipleChoices(difficulty.getMaxMultipleChoices())
				.build();
		
		return tc;
	}

}
