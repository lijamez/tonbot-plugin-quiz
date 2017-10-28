package net.tonbot.plugin.trivia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A {@link TriviaListener} wrapper which never throws exceptions which prevents
 * interruption of the {@link TriviaSession} if something goes wrong with the
 * notification (e.g. Discord related errors).
 */
class QuietTriviaListener implements TriviaListener {

	private static final Logger LOG = LoggerFactory.getLogger(QuietTriviaListener.class);

	private final TriviaListener listener;

	public QuietTriviaListener(TriviaListener listener) {
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
	}

	@Override
	public void onRoundStart(RoundStartEvent roundStartEvent) {
		runQuietly(() -> listener.onRoundStart(roundStartEvent));
	}

	@Override
	public void onRoundEnd(RoundEndEvent roundEndEvent) {
		runQuietly(() -> listener.onRoundEnd(roundEndEvent));
	}

	@Override
	public void onMultipleChoiceQuestionStart(MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent) {
		runQuietly(() -> listener.onMultipleChoiceQuestionStart(multipleChoiceQuestionStartEvent));
	}

	@Override
	public void onMultipleChoiceQuestionEnd(MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent) {
		runQuietly(() -> listener.onMultipleChoiceQuestionEnd(multipleChoiceQuestionEndEvent));
	}

	@Override
	public void onShortAnswerQuestionStart(ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent) {
		runQuietly(() -> listener.onShortAnswerQuestionStart(shortAnswerQuestionStartEvent));
	}

	@Override
	public void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent) {
		runQuietly(() -> listener.onShortAnswerQuestionEnd(shortAnswerQuestionEndEvent));
	}

	@Override
	public void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent) {
		runQuietly(() -> listener.onMusicIdQuestionStart(musicIdQuestionStartEvent));
	}

	@Override
	public void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent) {
		runQuietly(() -> listener.onMusicIdQuestionEnd(musicIdQuestionEndEvent));
	}

	@Override
	public void onAnswerCorrect(AnswerCorrectEvent answerCorrectEvent) {
		runQuietly(() -> listener.onAnswerCorrect(answerCorrectEvent));
	}

	@Override
	public void onAnswerIncorrect(AnswerIncorrectEvent answerIncorrectEvent) {
		runQuietly(() -> listener.onAnswerIncorrect(answerIncorrectEvent));
	}

	private void runQuietly(Runnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			LOG.warn("Listener has thrown an exception.", e);
		}
	}

}
