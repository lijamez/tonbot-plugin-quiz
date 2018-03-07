package net.tonbot.plugin.trivia.musicid;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.QuestionStartEvent;

@Data
@EqualsAndHashCode(callSuper = true)
public class MusicIdQuestionStartEvent extends QuestionStartEvent<MusicIdQuestion> {
	
	@Builder
	private MusicIdQuestionStartEvent(
			MusicIdQuestion question, 
			long questionNumber, 
			long totalQuestions,
			long maxDurationMs) {
		super(question, questionNumber, totalQuestions, maxDurationMs);
	}

}
