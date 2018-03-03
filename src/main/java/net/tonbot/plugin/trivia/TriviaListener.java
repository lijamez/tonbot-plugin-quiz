package net.tonbot.plugin.trivia;

import net.tonbot.plugin.trivia.musicid.MusicIdQuestionEndEvent;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionStartEvent;

public interface TriviaListener {
	
	void onCrash();
	
	/**
	 * Called as the round begins. This method should throw {@link TonbotBusinessException} if the listener is not prepared to start the round.
	 * @param roundStartEvent {@link RoundStartEvent}
	 * @throws TonbotBusinessException if the listener is not prepared to start the round.
	 */
	void onRoundStart(RoundStartEvent roundStartEvent);

	void onRoundEnd(RoundEndEvent roundEndEvent);
	
	/**
	 * Called when *any* message is received. That message may or may not be an answer.
	 * @param userMessageReceivedEvent {@link UserMessageReceivedEvent}.
	 */
	void onUserMessageReceived(UserMessageReceivedEvent userMessageReceivedEvent);

	void onMultipleChoiceQuestionStart(MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent);

	void onMultipleChoiceQuestionEnd(MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent);

	void onShortAnswerQuestionStart(ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent);

	void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent);

	void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent);

	void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent);

	void onAnswerCorrect(AnswerCorrectEvent answerCorrectEvent);

	void onAnswerIncorrect(AnswerIncorrectEvent answerIncorrectEvent);
}
