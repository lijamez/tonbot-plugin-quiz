package net.tonbot.plugin.trivia.model;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.musicid.SongProperty;

@Data
@EqualsAndHashCode(callSuper = true)
public class MusicIdQuestionTemplate extends QuestionTemplate {

	private final String audioPath;
	private final Map<SongProperty, SongPropertyData> properties;
	
	/**
	 * Constructor.
	 * @param points The number of points this question is worth.
	 * @param imagePaths Path to images. Non-null.
	 * @param audioPath Path to an audio file. Non-null
	 * @param properties Additional information pertaining to this song. Nullable.
	 */
	@Builder
	@JsonCreator
	public MusicIdQuestionTemplate(
			@JsonProperty("points") long points, 
			@JsonProperty("images") List<String> imagePaths,
			@JsonProperty("audio") String audioPath,
			@JsonProperty("askForProperties") Map<SongProperty, SongPropertyData> properties) {
		super(points, imagePaths);
		
		this.audioPath = Preconditions.checkNotNull(audioPath);
		
		this.properties = Preconditions.checkNotNull(properties, "properties must be non-null.");
		
		for (SongPropertyData propertyDatum : properties.values()) {
			Preconditions.checkNotNull(propertyDatum, "property keys must not map to null values.");
		}
	}

}
