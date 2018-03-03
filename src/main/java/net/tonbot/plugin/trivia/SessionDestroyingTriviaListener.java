package net.tonbot.plugin.trivia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import net.tonbot.common.TonbotBusinessException;
import net.tonbot.common.TonbotTechnicalFault;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionEndEvent;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionStartEvent;

/**
 * A {@link TriviaListener} wrapper which destroys the {@link TriviaSession} if an exception is thrown by the wrapped listener.
 */
class SessionDestroyingTriviaListener implements TriviaListener {

	private static final Logger LOG = LoggerFactory.getLogger(SessionDestroyingTriviaListener.class);

	private final TriviaListener listener;
	private final TriviaSession session;

	public SessionDestroyingTriviaListener(TriviaListener listener, TriviaSession session) {
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.session = Preconditions.checkNotNull(session, "session must be non-null.");
	}
	
	@Override
	public void onRoundStart(RoundStartEvent roundStartEvent) {
		// Since the real listener's onRoundStart method can throw TonbotBusinessExceptions if it is not 
		// prepared to start the round, that exception should be propagated back to the caller.
		// Any other exceptions will crash the session.
		try {
			listener.onRoundStart(roundStartEvent);
		} catch (TonbotBusinessException e) {
			throw e;
		} catch (Exception e) {
			handleException(e);
		}
	}

	@Override
	public void onRoundEnd(RoundEndEvent roundEndEvent) {
		run(() -> listener.onRoundEnd(roundEndEvent));
	}
	
	@Override
	public void onUserMessageReceived(UserMessageReceivedEvent userMessageReceivedEvent) {
		run(() -> listener.onUserMessageReceived(userMessageReceivedEvent));
	}

	@Override
	public void onMultipleChoiceQuestionStart(MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent) {
		run(() -> listener.onMultipleChoiceQuestionStart(multipleChoiceQuestionStartEvent));
	}

	@Override
	public void onMultipleChoiceQuestionEnd(MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent) {
		run(() -> listener.onMultipleChoiceQuestionEnd(multipleChoiceQuestionEndEvent));
	}

	@Override
	public void onShortAnswerQuestionStart(ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent) {
		run(() -> listener.onShortAnswerQuestionStart(shortAnswerQuestionStartEvent));
	}

	@Override
	public void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent) {
		run(() -> listener.onShortAnswerQuestionEnd(shortAnswerQuestionEndEvent));
	}

	@Override
	public void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent) {
		run(() -> listener.onMusicIdQuestionStart(musicIdQuestionStartEvent));
	}

	@Override
	public void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent) {
		run(() -> listener.onMusicIdQuestionEnd(musicIdQuestionEndEvent));
	}

	@Override
	public void onAnswerCorrect(AnswerCorrectEvent answerCorrectEvent) {
		run(() -> listener.onAnswerCorrect(answerCorrectEvent));
	}

	@Override
	public void onAnswerIncorrect(AnswerIncorrectEvent answerIncorrectEvent) {
		run(() -> listener.onAnswerIncorrect(answerIncorrectEvent));
	}
	
	@Override
	public void onCrash() {
		run(() -> listener.onCrash());
	}

	private void run(Runnable runnable) {
		try {
			runnable.run();
		} catch (Exception e) {
			handleException(e);
		}
	}
	
	private void handleException(Exception e) {
		LOG.error("Listener has thrown an exception.", e);
		session.destroy();
		
		try {
			listener.onCrash();
		} catch (Exception e2) { 
			LOG.error("Listener has thrown an exception when calling onCrash.", e);
		}
		
		throw new TonbotTechnicalFault("Trivia has ended abruptly due to an exception.", e);
	}
}