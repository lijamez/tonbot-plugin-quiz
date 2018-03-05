package net.tonbot.plugin.trivia.musicid;

import java.util.List;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.QuestionEndEvent;
import net.tonbot.plugin.trivia.Win;

@Data
@EqualsAndHashCode(callSuper = true)
public class MusicIdQuestionEndEvent extends QuestionEndEvent {

	private final SongProperty property;
	private final List<String> answers;
	
	private final SongMetadata songMetadata;

	/**
	 * 
	 * @param timedOut
	 *            Whether if the question timed out or not.
	 * @param win
	 *            The win details, if any. Nullable.
	 * @param answers
	 *            The list of acceptable answers. The first one is the "canonical" answer. Non-null, non-empty.
	 * @param songMetadata
	 *            Information about the song that was just played. Non-null.
	 *            
	 */
	@Builder
	private MusicIdQuestionEndEvent(
			boolean timedOut, 
			Win win, 
			SongProperty property,
			List<String> answers,
			SongMetadata songMetadata) {
		super(timedOut, win);

		this.property = Preconditions.checkNotNull(property, "property must be non-null.");
		this.answers = Preconditions.checkNotNull(answers, "answers must be non-null.");
		this.songMetadata = Preconditions.checkNotNull(songMetadata, "songMetadata must be non-null.");
	}

}
