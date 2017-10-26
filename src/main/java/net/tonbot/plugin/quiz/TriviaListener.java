package net.tonbot.plugin.quiz;

interface TriviaListener {

	void onRoundStart(RoundStartEvent roundStartEvent);
	
	void onRoundEnd(RoundEndEvent roundEndEvent);
	
	void onMultipleChoiceQuestionStart(MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent);
	
	void onMultipleChoiceQuestionEnd(MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent);
	
	void onShortAnswerQuestionStart(ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent);
	
	void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent);
	
	void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent);
	
	void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent);
}
