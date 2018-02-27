package net.tonbot.plugin.trivia.musicid;

import java.io.File;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.QuestionStartEvent;
import net.tonbot.plugin.trivia.model.QuestionTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
public class MusicIdQuestionStartEvent extends QuestionStartEvent {

	private final Tag tagToAsk;
	private final File audioFile;
	
	@Builder
	private MusicIdQuestionStartEvent(
			QuestionTemplate question, 
			long questionNumber, 
			long totalQuestions,
			long maxDurationMs, 
			File image,
			Tag tagToAsk,
			File audioFile) {
		super(question, questionNumber, totalQuestions, maxDurationMs, image);
		
		this.tagToAsk = Preconditions.checkNotNull(tagToAsk, "tagToAsk must be non-null.");
		this.audioFile = Preconditions.checkNotNull(audioFile, "audioFile must be non-null.");
	}

}
