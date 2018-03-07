package net.tonbot.plugin.trivia.musicid;

import java.io.File;
import java.util.List;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.Question;

/**
 * The manifestation of a particular music ID question.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MusicIdQuestion extends Question {

	private final SongProperty propertyToAsk;
	private final List<String> answers;
	private final File audioFile;
	
	@Builder
	private MusicIdQuestion(long points, File image, SongProperty propertyToAsk, List<String> answers, File audioFile) {
		super(points, image);
		
		this.propertyToAsk = Preconditions.checkNotNull(propertyToAsk, "propertyToAsk must be non-null.");
		
		this.answers = Preconditions.checkNotNull(answers, "answers must be non-null.");
		Preconditions.checkArgument(!answers.isEmpty(), "answers must not be empty.");
		
		this.audioFile = Preconditions.checkNotNull(audioFile, "audioFile must be non-null.");
		Preconditions.checkArgument(audioFile.exists(), "audioFile must exist.");
	}
}
