package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.QuestionTemplate;

/**
 * This class is thread safe.
 */
class TriviaSession {

	private static final long PRE_QUESTION_DELAY_MS = 4000;

	private final QuietTriviaListener listener;
	private final LoadedTrivia trivia;
	private final List<QuestionTemplate> availableQuestionTemplates;
	private final TriviaConfiguration config;
	private final Random random;
	private final QuestionHandlers questionHandlers;
	private final long totalQuestionsToAsk;

	private long numQuestionsAsked;

	private TriviaSessionState state;
	private QuestionHandler currentQuestionHandler;

	private Scorekeeper scorekeeper;

	private ScheduledTaskRunner scheduledTaskRunner;
	private ReentrantLock lock;

	public TriviaSession(TriviaListener listener, LoadedTrivia trivia, TriviaConfiguration config, Random random, QuestionHandlers questionHandlers) {

		Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.listener = new QuietTriviaListener(listener);
		this.trivia = Preconditions.checkNotNull(trivia, "trivia must be non-null.");
		this.config = Preconditions.checkNotNull(config, "config must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
		this.questionHandlers = Preconditions.checkNotNull(questionHandlers, "questionHandlers must be non-null.");

		this.availableQuestionTemplates = new ArrayList<>(
				this.trivia.getTriviaTopic().getQuestionBundle().getQuestionTemplates());
		this.totalQuestionsToAsk = Math.min(config.getMaxQuestions(), availableQuestionTemplates.size());

		this.numQuestionsAsked = 0;
		this.state = TriviaSessionState.NOT_STARTED;
		this.currentQuestionHandler = null;
		this.scorekeeper = new Scorekeeper(config.getScoreDecayFactor());
		this.scheduledTaskRunner = new ScheduledTaskRunner();
		this.lock = new ReentrantLock();
	}

	public void start() {
		lock.lock();
		try {
			Preconditions.checkState(this.state == TriviaSessionState.NOT_STARTED,
					"The session has already started or has already ended.");

			RoundStartEvent roundStartEvent = RoundStartEvent.builder()
					.triviaMetadata(trivia.getTriviaTopic().getMetadata())
					.difficultyName(config.getDifficultyName()).build();

			listener.onRoundStart(roundStartEvent);

			// This delay prevents a race condition from occurring which causes the trivia
			// session to treat the user's "play" command as an input in takeInput().
			loadNextQuestionOrEnd();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This method is to be called by a separate thread to time out a question.
	 */
	private void timeoutQuestion() {
		lock.lock();
		try {
			Preconditions.checkState(this.state == TriviaSessionState.WAITING_FOR_ANSWER,
					"The session has not yet started or has already ended.");

			if (currentQuestionHandler != null) {
				currentQuestionHandler.notifyEnd(null, 0, 0);
			}

			loadNextQuestionOrEnd();
		} finally {
			lock.unlock();
		}
	}

	private void loadNextQuestionOrEnd() {
		this.state = TriviaSessionState.LOADING_NEXT_QUESTION;
		boolean hasNextQuestion = totalQuestionsToAsk - numQuestionsAsked > 0;
		Runnable runnable;
		if (hasNextQuestion) {
			runnable = () -> {
				lock.lock();
				try {
					state = TriviaSessionState.WAITING_FOR_ANSWER;
					QuestionTemplate nextQuestion = pickRandomElement(this.availableQuestionTemplates);
					this.availableQuestionTemplates.remove(nextQuestion);
					this.numQuestionsAsked++;

					File imageFile = getRandomImageFile(nextQuestion);

					this.scorekeeper.setupQuestion(nextQuestion.getPoints());
					this.currentQuestionHandler = questionHandlers.get(nextQuestion, config, listener, trivia);
					this.currentQuestionHandler.notifyStart(this.numQuestionsAsked, this.totalQuestionsToAsk,
							config.getDefaultTimePerQuestion(), imageFile);
					this.scheduledTaskRunner.replaceSchedule(() -> {
						try {
							timeoutQuestion();
						} catch (IllegalStateException e) {
							// Timeout call was a bit late, and the game must've ended.
							// Hence this is ignorable.
						}
					}, config.getDefaultTimePerQuestion(), TimeUnit.MILLISECONDS);
				} finally {
					lock.unlock();
				}
			};
		} else {
			this.scheduledTaskRunner.cancel();

			this.currentQuestionHandler = null;
			this.state = TriviaSessionState.ENDED;

			runnable = () -> {
				RoundEndEvent roundEndEvent = RoundEndEvent.builder().scores(scorekeeper.getScores()).build();
				this.listener.onRoundEnd(roundEndEvent);
			};
		}

		this.scheduledTaskRunner.replaceSchedule(runnable, PRE_QUESTION_DELAY_MS, TimeUnit.MILLISECONDS);
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

		lock.lock();
		try {
			if (this.state != TriviaSessionState.WAITING_FOR_ANSWER) {
				return;
			}

			Optional<Boolean> correct = this.currentQuestionHandler.checkCorrectness(userMessage);

			if (!correct.isPresent()) {
				return;
			}

			if (correct.get()) {
				this.scheduledTaskRunner.cancel();
				long incorrectAttempts = this.scorekeeper.getIncorrectAnswers(userMessage.getUserId());
				long awardedPoints = this.scorekeeper.logCorrectAnswerAndAdvance(userMessage.getUserId());

				AnswerCorrectEvent answerCorrectEvent = AnswerCorrectEvent.builder()
						.messageId(userMessage.getMessageId()).build();
				this.listener.onAnswerCorrect(answerCorrectEvent);
				this.currentQuestionHandler.notifyEnd(userMessage, awardedPoints, incorrectAttempts);

				loadNextQuestionOrEnd();
			} else {
				// This user participated, so add them to the scores map if they are not already
				// there.
				this.scorekeeper.logIncorrectAnswer(userMessage.getUserId());

				AnswerIncorrectEvent event = AnswerIncorrectEvent.builder().messageId(userMessage.getMessageId())
						.build();
				listener.onAnswerIncorrect(event);
			}
		} finally {
			lock.unlock();
		}

	}

	/**
	 * Ends the session, which fires an onRoundEnd event. No-op if this session has
	 * already ended.
	 */
	public void end() {
		lock.lock();
		try {
			if (this.state == TriviaSessionState.ENDED) {
				return;
			}

			this.scheduledTaskRunner.cancel();
			this.state = TriviaSessionState.ENDED;
			RoundEndEvent roundEndEvent = RoundEndEvent.builder().scores(this.scorekeeper.getScores()).build();
			listener.onRoundEnd(roundEndEvent);
		} finally {
			lock.unlock();
		}

	}

	private File getRandomImageFile(QuestionTemplate q) {
		File imageFile = null;
		if (!q.getImagePaths().isEmpty()) {
			String imagePath = q.getImagePaths().get(random.nextInt(q.getImagePaths().size()));
			imageFile = new File(trivia.getTriviaTopicDir(), imagePath);
		}

		return imageFile;
	}

	private <T> T pickRandomElement(List<T> list) {
		int randomIndex = random.nextInt(list.size());

		return list.get(randomIndex);
	}

	private static enum TriviaSessionState {
		/**
		 * The initial state. May be transitioned into the {@code WAITING_FOR_ANSWER}
		 * state.
		 */
		NOT_STARTED,

		/**
		 * The state where questions are being asked and users should respond to them.
		 * May be transitioned into the {@code LOADING_NEXT_QUESTION} or the
		 * {@code ENDED} state.
		 */
		WAITING_FOR_ANSWER,

		/**
		 * The pause between the ending of one question and the beginning of another.
		 * May be transitioned into the {@code WAITING_FOR_ANSWER} state.
		 */
		LOADING_NEXT_QUESTION,

		/**
		 * The state where all questions have been asked and the trivia session has
		 * concluded. It's not possible to transition out of this state.
		 */
		ENDED
	}
}
