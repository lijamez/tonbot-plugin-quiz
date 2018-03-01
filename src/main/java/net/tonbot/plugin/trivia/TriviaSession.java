package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import net.tonbot.common.TonbotBusinessException;
import net.tonbot.plugin.trivia.model.MusicIdQuestionTemplate;
import net.tonbot.plugin.trivia.model.QuestionTemplate;

/**
 * This class is thread safe.
 */
class TriviaSession {
	
	private static final Logger LOG = LoggerFactory.getLogger(TriviaSession.class);

	private static final long ROUND_START_DELAY_MS = 10000;
	private static final long PRE_QUESTION_DELAY_MS = 5000;
	
	private final TriviaSessionManager triviaSessionManager;
	
	private final SessionDestroyingTriviaListener listener;
	private final LoadedTrivia trivia;
	private final List<QuestionTemplate> availableQuestionTemplates;
	private final TriviaConfiguration config;
	private final Random random;
	private final QuestionHandlers questionHandlers;
	private final long totalQuestionsToAsk;

	private TriviaSessionState state;
	private QuestionHandler currentQuestionHandler;

	private Scorekeeper scorekeeper;

	private ScheduledTaskRunner scheduledTaskRunner;
	private ReentrantLock lock;

	public TriviaSession(
			TriviaSessionManager triviaSessionManager,
			TriviaListener listener, 
			LoadedTrivia trivia, 
			TriviaConfiguration config, 
			DifficultyBasedQuestionSelector questionSelector,
			Random random, 
			QuestionHandlers questionHandlers) {
		
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager, "triviaSessionManager must be non-null.");
		Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.listener = new SessionDestroyingTriviaListener(listener, this);
		this.trivia = Preconditions.checkNotNull(trivia, "trivia must be non-null.");
		this.config = Preconditions.checkNotNull(config, "config must be non-null.");
		Preconditions.checkNotNull(questionSelector, "questionSelector must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
		this.questionHandlers = Preconditions.checkNotNull(questionHandlers, "questionHandlers must be non-null.");
		
		this.availableQuestionTemplates = questionSelector.pick(
				this.trivia.getTriviaTopic().getQuestionBundle().getQuestionTemplates(), 
				config.getDifficulty(), 
				config.getMaxQuestions());
		
		this.totalQuestionsToAsk = availableQuestionTemplates.size();

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
			
			boolean hasAudio = availableQuestionTemplates.stream()
				.filter(qt -> (qt instanceof MusicIdQuestionTemplate))
				.findAny()
				.isPresent();

			RoundStartEvent roundStartEvent = RoundStartEvent.builder()
					.triviaMetadata(trivia.getTriviaTopic().getMetadata())
					.difficultyName(config.getDifficulty().getFriendlyName())
					.startingInMs(ROUND_START_DELAY_MS)
					.hasAudio(hasAudio)
					.build();
			
			try {
				listener.onRoundStart(roundStartEvent);
			} catch (TonbotBusinessException e) {
				destroy();
				throw e;
			}

			// This delay prevents a race condition from occurring which causes the trivia
			// session to treat the user's "play" command as an input in takeInput().
			loadNextQuestionOrEnd(ROUND_START_DELAY_MS);
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

			loadNextQuestionOrEnd(PRE_QUESTION_DELAY_MS);
		} finally {
			lock.unlock();
		}
	}

	private void loadNextQuestionOrEnd(long delayMs) {
		this.scorekeeper.endQuestion();
		
		this.state = TriviaSessionState.LOADING_NEXT_QUESTION;
		
		
		
		boolean hasNextQuestion = !availableQuestionTemplates.isEmpty();
		Runnable runnable;
		if (hasNextQuestion) {
			runnable = () -> {
				lock.lock();
				try {
					state = TriviaSessionState.WAITING_FOR_ANSWER;
					QuestionTemplate nextQuestion = this.availableQuestionTemplates.remove(0);

					File imageFile = getRandomImageFile(nextQuestion);

					this.scorekeeper.setupQuestion(nextQuestion.getPoints());
					this.currentQuestionHandler = questionHandlers.get(nextQuestion, config, listener, trivia);
					
					long currentQuestionNumber = this.totalQuestionsToAsk - this.availableQuestionTemplates.size();
					
					this.currentQuestionHandler.notifyStart(currentQuestionNumber, this.totalQuestionsToAsk,
							config.getDefaultTimePerQuestion(), imageFile);
					this.scheduledTaskRunner.replaceSchedule(() -> {
						try {
							timeoutQuestion();
						} catch (IllegalStateException e) {
							// Timeout call was a bit late, and the game must've ended.
							// Hence this is ignorable.
						} catch (Exception e) {
							LOG.error("Question timeout task has unexpectedly thrown an exception.", e);
						}
					}, config.getDefaultTimePerQuestion(), TimeUnit.MILLISECONDS);
				} catch (Exception e) {
					LOG.error("Unable to set up next question.", e);
					throw e;
				} finally {
					lock.unlock();
				}
			};
		} else {
			runnable = () -> {
				try {
					end();
				} catch (Exception e) {
					LOG.error("Round ending task has unexpectedly thrown an exception.", e);
				}
				
			};
		}

		this.scheduledTaskRunner.replaceSchedule(runnable, delayMs, TimeUnit.MILLISECONDS);
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
				long incorrectAnswers = this.scorekeeper.getQuestionRecord(userMessage.getUserId())
						.getIncorrectAnswers();
				long awardedPoints = this.scorekeeper.logCorrectAnswer(userMessage.getUserId());

				AnswerCorrectEvent answerCorrectEvent = AnswerCorrectEvent.builder()
						.messageId(userMessage.getMessageId()).build();
				this.listener.onAnswerCorrect(answerCorrectEvent);
				this.currentQuestionHandler.notifyEnd(userMessage, awardedPoints, incorrectAnswers);

				loadNextQuestionOrEnd(PRE_QUESTION_DELAY_MS);
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
	 * Ends the session nicely, which fires an onRoundEnd event. 
	 * No-op if this session has already ended.
	 */
	void end() {
		lock.lock();
		try {
			if (this.state == TriviaSessionState.ENDED) {
				return;
			} else if (this.state == TriviaSessionState.NOT_STARTED) {
				this.state = TriviaSessionState.ENDED;
				return;
			}
			
			this.scorekeeper.endQuestion();

			this.scheduledTaskRunner.cancel();
			this.state = TriviaSessionState.ENDED;

			triviaSessionManager.sessionHasEnded(this);
			
			RoundEndEvent roundEndEvent = RoundEndEvent.builder()
					.scorekeepingRecords(this.scorekeeper.getRecords())
					.loadedTrivia(trivia)
					.triviaConfig(config)
					.build();
			
			listener.onRoundEnd(roundEndEvent);
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Abruptly ends the session. No events are fired.
	 */
	void destroy() {
		if (this.state == TriviaSessionState.ENDED) {
			return;
		}
		
		// Something went horribly wrong. Shut down everything.
		this.scheduledTaskRunner.shutdownNow();
		this.state = TriviaSessionState.ENDED;
		
		triviaSessionManager.sessionHasEnded(this);
	}

	private File getRandomImageFile(QuestionTemplate q) {
		File imageFile = null;
		if (!q.getImagePaths().isEmpty()) {
			String imagePath = q.getImagePaths().get(random.nextInt(q.getImagePaths().size()));
			imageFile = new File(trivia.getTriviaTopicDir(), imagePath);
		}

		return imageFile;
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
