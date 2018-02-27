package net.tonbot.plugin.trivia.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.musicid.Tag;

@Data
@EqualsAndHashCode(callSuper = true)
public class MusicIdQuestionTemplate extends QuestionTemplate {

	private final String audioPath;
	private final Set<Tag> tags;
	
	/**
	 * Constructor.
	 * @param points The number of points this question is worth.
	 * @param imagePaths Path to images. Non-null.
	 * @param audioPath Path to an audio file. Non-null
	 * @param tags The tags to ask the user. Must contain at least one tag. Non-null.
	 */
	@Builder
	@JsonCreator
	public MusicIdQuestionTemplate(
			@JsonProperty("points") long points, 
			@JsonProperty("images") List<String> imagePaths,
			@JsonProperty("audio") String audioPath,
			@JsonProperty("tags") Set<Tag> tags) {
		super(points, imagePaths);
		
		this.audioPath = Preconditions.checkNotNull(audioPath);
		
		Preconditions.checkNotNull(tags);
		Preconditions.checkArgument(!tags.isEmpty());
		this.tags = tags;
	}

}
