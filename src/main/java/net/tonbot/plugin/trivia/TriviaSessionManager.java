package net.tonbot.plugin.trivia;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.TriviaTopic;

class TriviaSessionManager {

	private final TriviaLibrary triviaLibrary;
	private final Random random;
	private final QuestionHandlers questionHandlers;

	private final ConcurrentHashMap<TriviaSessionKey, TriviaSession> sessions;

	@Inject
	public TriviaSessionManager(TriviaLibrary triviaLibrary, Random random, QuestionHandlers questionHandlers) {
		this.triviaLibrary = Preconditions.checkNotNull(triviaLibrary, "triviaLibrary must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
		this.questionHandlers = Preconditions.checkNotNull(questionHandlers, "questionHandlers must be non-null.");

		this.sessions = new ConcurrentHashMap<>();
	}

	public Optional<TriviaSession> getSession(TriviaSessionKey sessionKey) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		return Optional.ofNullable(this.sessions.get(sessionKey));
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
	public TriviaSession createSession(TriviaSessionKey sessionKey, String triviaTopicName, Difficulty difficulty,
			TriviaListener listener) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		Preconditions.checkNotNull(triviaTopicName, "triviaTopicName must be non-null.");
		Preconditions.checkNotNull(difficulty, "difficulty must be non-null.");

		LoadedTrivia loadedTrivia = triviaLibrary.getTrivia(triviaTopicName)
				.orElseThrow(() -> new InvalidTopicException("Trivia topic " + triviaTopicName + " is not valid."));
		TriviaConfiguration triviaConfig = getConfigFor(loadedTrivia.getTriviaTopic(), difficulty);
		TriviaSession triviaSession = new TriviaSession(listener, loadedTrivia, triviaConfig, random, questionHandlers);
		TriviaSession oldSession = this.sessions.put(sessionKey, triviaSession);

		if (oldSession != null) {
			oldSession.end();
		}

		triviaSession.start();

		return triviaSession;
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
