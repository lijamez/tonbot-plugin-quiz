package net.tonbot.plugin.trivia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.Question;
import net.tonbot.plugin.trivia.model.TriviaPack;

//TODO: All calls to listener methods should not interrupt session.
public class TriviaSession {

	private final TriviaListener listener;
	private final TriviaPack triviaPack;
	private final List<Question> availableQuestions;
	private final TriviaConfiguration config;
	private final ThreadLocalRandom random;
	private final long totalQuestionsToAsk;

	private long numQuestionsAsked;

	private TriviaSessionState state;
	private QuestionHandler currentQuestionHandler;

	private Map<Long, Long> score;

	public TriviaSession(
			TriviaListener listener,
			TriviaPack triviaPack,
			TriviaConfiguration config,
			ThreadLocalRandom random) {

		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.triviaPack = Preconditions.checkNotNull(triviaPack, "triviaPack must be non-null.");
		this.config = Preconditions.checkNotNull(config, "config must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");

		this.availableQuestions = new ArrayList<>(this.triviaPack.getQuestionBundle().getQuestions());
		this.totalQuestionsToAsk = Math.min(config.getMaxQuestions(), availableQuestions.size());

		this.numQuestionsAsked = 0;
		this.state = TriviaSessionState.NOT_STARTED;
		this.currentQuestionHandler = null;
		this.score = new HashMap<>();
		// TODO: Make this class thread safe.
		// TODO: Create a thread which times out questions.
	}

	public void start() {
		Preconditions.checkState(
				this.state == TriviaSessionState.NOT_STARTED, "The session has already started or has already ended.");

		RoundStartEvent roundStartEvent = RoundStartEvent.builder()
				.triviaMetadata(triviaPack.getMetadata())
				.build();

		listener.onRoundStart(roundStartEvent);
		this.state = TriviaSessionState.STARTED;
		nextQuestionOrEnd();
	}

	/**
	 * This method is to be called by a separate thread to time out a question.
	 */
	public void timeoutQuestion() {
		Preconditions.checkState(
				this.state == TriviaSessionState.STARTED, "The session has not yet started or has already ended.");

		if (currentQuestionHandler != null) {
			currentQuestionHandler.notifyEnd(null);
		}

		nextQuestionOrEnd();
	}

	private void nextQuestionOrEnd() {
		if (totalQuestionsToAsk - numQuestionsAsked > 0) {
			Question nextQuestion = pickRandomElement(this.availableQuestions);
			this.availableQuestions.remove(nextQuestion);
			this.numQuestionsAsked++;

			this.currentQuestionHandler = QuestionHandlers.get(nextQuestion, listener);
			this.currentQuestionHandler.notifyStart(
					this.numQuestionsAsked,
					this.totalQuestionsToAsk,
					config.getQuestionTimeSeconds());
			// TODO: Reset the timer thread.
		} else {
			this.currentQuestionHandler = null;
			this.state = TriviaSessionState.ENDED;
			RoundEndEvent roundEndEvent = RoundEndEvent.builder()
					.scores(score)
					.build();
			this.listener.onRoundEnd(roundEndEvent);
			// TODO: Kill the timer thread.
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

			AnswerCorrectEvent answerCorrectEvent = AnswerCorrectEvent.builder()
					.messageId(userMessage.getMessageId())
					.build();
			this.listener.onAnswerCorrect(answerCorrectEvent);
			this.currentQuestionHandler.notifyEnd(userMessage);

			nextQuestionOrEnd();
		} else {
			AnswerIncorrectEvent event = AnswerIncorrectEvent.builder()
					.messageId(userMessage.getMessageId())
					.build();
			listener.onAnswerIncorrect(event);
		}
	}

	public void end() {
		// TODO: Kill the timer thread.
		this.state = TriviaSessionState.ENDED;
		RoundEndEvent roundEndEvent = RoundEndEvent.builder()
				.scores(score)
				.build();
		listener.onRoundEnd(roundEndEvent);
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
