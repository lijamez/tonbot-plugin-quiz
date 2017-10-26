package net.tonbot.plugin.quiz;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.quiz.model.Question;
import net.tonbot.plugin.quiz.model.TriviaMetadata;

public class TriviaSession {

	private final TriviaListener listener;
	private final TriviaMetadata metadata;
	private final List<Question> availableQuestions;
	private final TriviaConfiguration config;
	private final ThreadLocalRandom random;

	private TriviaSessionState state;
	private QuestionHandler currentQuestionHandler;

	// The number of questions to ask the users.
	private int questionsRemaining;

	private Map<Long, Long> score;

	public TriviaSession(
			TriviaListener listener,
			TriviaMetadata metadata,
			List<Question> questions,
			TriviaConfiguration config,
			ThreadLocalRandom random) {

		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.metadata = Preconditions.checkNotNull(metadata, "metadata must be non-null.");
		this.availableQuestions = Preconditions.checkNotNull(questions, "questions must be non-null.");
		this.config = Preconditions.checkNotNull(config, "config must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");

		this.questionsRemaining = Math.min(config.getMaxQuestions(), availableQuestions.size());
		this.state = TriviaSessionState.NOT_STARTED;
		this.currentQuestionHandler = null;
		this.score = new HashMap<>();
	}

	public void start() {
		Preconditions.checkState(
				this.state == TriviaSessionState.NOT_STARTED, "The session has already started or has already ended.");

		RoundStartEvent roundStartEvent = RoundStartEvent.builder()
				.triviaMetadata(metadata)
				.build();

		listener.onRoundStart(roundStartEvent);
		this.state = TriviaSessionState.STARTED;
		nextQuestionOrEnd();
	}

	public void timeoutQuestion() {
		Preconditions.checkState(
				this.state == TriviaSessionState.STARTED, "The session has not yet started or has already ended.");

		if (currentQuestionHandler != null) {
			currentQuestionHandler.notifyEnd(null);
		}

		nextQuestionOrEnd();
	}

	private void nextQuestionOrEnd() {
		if (this.questionsRemaining > 0) {
			Question nextQuestion = pickRandomElement(this.availableQuestions);
			this.availableQuestions.remove(nextQuestion);
			this.questionsRemaining--;

			this.currentQuestionHandler = QuestionHandlers.get(nextQuestion, listener);
			this.currentQuestionHandler.notifyStart(config.getQuestionTimeSeconds());
		} else {
			this.currentQuestionHandler = null;
			this.state = TriviaSessionState.ENDED;
			RoundEndEvent roundEndEvent = RoundEndEvent.builder()
					.score(score)
					.build();
			this.listener.onRoundEnd(roundEndEvent);
		}
	}

	/**
	 * Takes in a message and checks if it answers the current question. No-op if
	 * the session has not yet started or has ended.
	 * 
	 * @param userMessage
	 *            {@link UserMessage}. Non-null.
	 */
	public void takeInput(UserMessage userMessage) {
		Preconditions.checkNotNull(userMessage, "userMessage must be non-null.");

		if (this.state != TriviaSessionState.STARTED) {
			return;
		}

		Optional<Boolean> correct = this.currentQuestionHandler.checkCorrectness(userMessage);

		if (!correct.isPresent()) {
			return;
		}

		if (correct.get()) {
			// Update the score
			Question question = this.currentQuestionHandler.getQuestion();
			long newUserScore = this.score.getOrDefault(userMessage.getUserId(), 0L) + question.getPoints();
			this.score.put(userMessage.getUserId(), newUserScore);

			this.currentQuestionHandler.notifyEnd(userMessage);
		} else {
			AnswerIncorrectEvent event = AnswerIncorrectEvent.builder()
					.messageId(userMessage.getMessageId())
					.build();
			listener.onAnswerIncorrect(event);
		}
	}

	private <T> T pickRandomElement(List<T> list) {
		int randomIndex = random.nextInt(0, list.size());

		return list.get(randomIndex);
	}

	private static enum TriviaSessionState {
		NOT_STARTED,
		STARTED,
		ENDED
	}
}
