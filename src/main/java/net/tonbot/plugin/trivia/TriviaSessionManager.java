package net.tonbot.plugin.trivia;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.TriviaTopic;

class TriviaSessionManager {

	private final TriviaLibrary triviaLibrary;
	private final Random random;
	private final QuestionHandlers questionHandlers;
	
	private final Map<TriviaSessionKey, TriviaSession> sessions;
	private final ReadWriteLock lock;

	@Inject
	public TriviaSessionManager(
			TriviaLibrary triviaLibrary, 
			Random random, 
			QuestionHandlers questionHandlers) {
		this.triviaLibrary = Preconditions.checkNotNull(triviaLibrary, "triviaLibrary must be non-null.");
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
	 * Creates a trivia session for the given guild. Any existing sessions will be
	 * replaced, but not before firing their RoundEndEvent.
	 * 
	 * @param sessionKey
	 *            {@link TriviaSessionKey}. Non-null.
	 * @param triviaTopicName
	 *            The trivia topic name. Must be the name of a valid trivia topic.
	 *            Non-null.
	 * @param difficulty
	 *            The {@link Difficulty.} Non-null.
	 * @return The new {@link TriviaSession}.
	 * @throws InvalidTopicException
	 *             if the specified trivia topic is not valid.
	 */
	public synchronized TriviaSession createSession(
			TriviaSessionKey sessionKey, 
			String triviaTopicName, 
			Difficulty difficulty,
			TriviaListener listener) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		Preconditions.checkNotNull(triviaTopicName, "triviaTopicName must be non-null.");
		Preconditions.checkNotNull(difficulty, "difficulty must be non-null.");

		LoadedTrivia loadedTrivia = triviaLibrary.getTrivia(triviaTopicName)
				.orElseThrow(() -> new InvalidTopicException("Trivia topic " + triviaTopicName + " is not valid."));
		TriviaConfiguration triviaConfig = getConfigFor(loadedTrivia.getTriviaTopic(), difficulty);
		TriviaSession triviaSession = new TriviaSession(listener, loadedTrivia, triviaConfig, random, questionHandlers);
		
		lock.writeLock().lock();
		try {
			TriviaSession oldSession = this.sessions.put(sessionKey, triviaSession);

			if (oldSession != null) {
				oldSession.end();
			}

			triviaSession.start();
		} finally {
			lock.writeLock().unlock();
		}

		return triviaSession;
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

	private TriviaConfiguration getConfigFor(TriviaTopic topic, Difficulty difficulty) {
		Preconditions.checkNotNull(topic, "topic must be non-null.");
		Preconditions.checkNotNull(difficulty, "difficulty must be non-null.");
		
		int maxQuestions = topic.getMetadata().getDefaultQuestionsPerRound();
		long defaultTimePerQuestion = topic.getMetadata().getDefaultTimePerQuestion();
		
		TriviaConfiguration tc = TriviaConfiguration.builder()
				.maxQuestions(maxQuestions)
				.defaultTimePerQuestion(defaultTimePerQuestion)
				.difficultyName(difficulty.getFriendlyName())
				.scoreDecayFactor(difficulty.getScoreDecayFactor())
				.maxMultipleChoices(difficulty.getMaxMultipleChoices())
				.build();
		
		return tc;
	}

}
