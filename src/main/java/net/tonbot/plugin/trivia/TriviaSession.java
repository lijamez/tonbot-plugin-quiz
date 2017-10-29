package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.QuestionTemplate;

/**
 * This class is thread safe.
 */
class TriviaSession {

	private static final long START_DELAY_SECONDS = 3;

	private final QuietTriviaListener listener;
	private final LoadedTrivia trivia;
	private final List<QuestionTemplate> availableQuestions;
	private final TriviaConfiguration config;
	private final Random random;
	private final long totalQuestionsToAsk;

	private long numQuestionsAsked;

	private TriviaSessionState state;
	private QuestionHandler currentQuestionHandler;

	private Scorekeeper scorekeeper;

	private Timer startTimer;
	private TriviaQuestionTimer timer;
	private ReentrantLock lock;

	public TriviaSession(
			TriviaListener listener,
			LoadedTrivia trivia,
			TriviaConfiguration config,
			Random random) {

		Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.listener = new QuietTriviaListener(listener);
		this.trivia = Preconditions.checkNotNull(trivia, "trivia must be non-null.");
		this.config = Preconditions.checkNotNull(config, "config must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");

		this.availableQuestions = new ArrayList<>(
				this.trivia.getTriviaPack().getQuestionBundle().getQuestionTemplates());
		this.totalQuestionsToAsk = Math.min(config.getMaxQuestions(), availableQuestions.size());

		this.numQuestionsAsked = 0;
		this.state = TriviaSessionState.NOT_STARTED;
		this.currentQuestionHandler = null;
		this.scorekeeper = new Scorekeeper();
		this.startTimer = new Timer();
		this.timer = new TriviaQuestionTimer();
		this.lock = new ReentrantLock();
	}

	public void start() {
		Preconditions.checkState(
				this.state == TriviaSessionState.NOT_STARTED, "The session has already started or has already ended.");

		lock.lock();
		try {
			RoundStartEvent roundStartEvent = RoundStartEvent.builder()
					.triviaMetadata(trivia.getTriviaPack().getMetadata())
					.startingInSeconds(START_DELAY_SECONDS)
					.build();

			listener.onRoundStart(roundStartEvent);

			// This delay prevents a race condition from occurring which causes the trivia
			// session to treat the user's "play" command as an input in takeInput().
			this.state = TriviaSessionState.STARTING;
			this.startTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					lock.lock();
					try {
						state = TriviaSessionState.STARTED;
						nextQuestionOrEnd();
					} finally {
						lock.unlock();
					}
				}

			}, START_DELAY_SECONDS * 1000);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * This method is to be called by a separate thread to time out a question.
	 */
	public void timeoutQuestion() {
		Preconditions.checkState(
				this.state == TriviaSessionState.STARTED, "The session has not yet started or has already ended.");

		lock.lock();
		try {
			if (currentQuestionHandler != null) {
				currentQuestionHandler.notifyEnd(null, 0, 0);
			}

			nextQuestionOrEnd();
		} finally {
			lock.unlock();
		}
	}

	private void nextQuestionOrEnd() {
		if (totalQuestionsToAsk - numQuestionsAsked > 0) {
			QuestionTemplate nextQuestion = pickRandomElement(this.availableQuestions);
			this.availableQuestions.remove(nextQuestion);
			this.numQuestionsAsked++;

			File imageFile = getRandomImageFile(nextQuestion);

			this.scorekeeper.setupQuestion(nextQuestion.getPoints());
			this.currentQuestionHandler = QuestionHandlers.get(nextQuestion, listener);
			this.currentQuestionHandler.notifyStart(
					this.numQuestionsAsked,
					this.totalQuestionsToAsk,
					config.getQuestionTimeSeconds(),
					imageFile);
			this.timer.replaceSchedule(() -> timeoutQuestion(), config.getQuestionTimeSeconds() * 1000);
		} else {
			this.timer.cancel();

			this.currentQuestionHandler = null;
			this.state = TriviaSessionState.ENDED;
			RoundEndEvent roundEndEvent = RoundEndEvent.builder()
					.scores(scorekeeper.getScores())
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

		lock.lock();
		try {
			if (this.state != TriviaSessionState.STARTED) {
				return;
			}

			Optional<Boolean> correct = this.currentQuestionHandler.checkCorrectness(userMessage);

			if (!correct.isPresent()) {
				return;
			}

			if (correct.get()) {
				this.timer.cancel();
				long incorrectAttempts = this.scorekeeper.getIncorrectAnswers(userMessage.getUserId());
				long awardedPoints = this.scorekeeper.logCorrectAnswerAndAdvance(userMessage.getUserId());

				AnswerCorrectEvent answerCorrectEvent = AnswerCorrectEvent.builder()
						.messageId(userMessage.getMessageId())
						.build();
				this.listener.onAnswerCorrect(answerCorrectEvent);
				this.currentQuestionHandler.notifyEnd(userMessage, awardedPoints, incorrectAttempts);

				nextQuestionOrEnd();
			} else {
				// This user participated, so add them to the scores map if they are not already
				// there.
				this.scorekeeper.logIncorrectAnswer(userMessage.getUserId());

				AnswerIncorrectEvent event = AnswerIncorrectEvent.builder()
						.messageId(userMessage.getMessageId())
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

			this.timer.cancel();
			this.state = TriviaSessionState.ENDED;
			RoundEndEvent roundEndEvent = RoundEndEvent.builder()
					.scores(this.scorekeeper.getScores())
					.build();
			listener.onRoundEnd(roundEndEvent);
		} finally {
			lock.unlock();
		}

	}

	private File getRandomImageFile(QuestionTemplate q) {
		File imageFile = null;
		if (!q.getImagePaths().isEmpty()) {
			String imagePath = q.getImagePaths().get(random.nextInt(q.getImagePaths().size()));
			imageFile = new File(trivia.getTriviaPackDir(), imagePath);
		}

		return imageFile;
	}

	private <T> T pickRandomElement(List<T> list) {
		int randomIndex = random.nextInt(list.size());

		return list.get(randomIndex);
	}

	private static enum TriviaSessionState {
		/**
		 * The initial state. May be transitioned into the {@code STARTING} state.
		 */
		NOT_STARTED,

		/**
		 * The pause between NOT_STARTED and STARTED. This state allows users to prepare
		 * and prevents a race condition from occurring where the "trivia play" command
		 * would be treated as input. May be transitioned into the {@code STARTED}
		 * state.
		 */
		STARTING,

		/**
		 * The state where questions are being asked and users should respond to them.
		 * May be transitioned into the {@code ENDED} state.
		 */
		STARTED,

		/**
		 * The state where all questions have been asked and the trivia session has
		 * concluded. It's not possible to transition out of this state.
		 */
		ENDED
	}
}
