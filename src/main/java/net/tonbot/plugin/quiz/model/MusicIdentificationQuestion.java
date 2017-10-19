package net.tonbot.plugin.quiz.model;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MusicIdentificationQuestion extends Question {

	private final String trackPath;
	private final Set<TrackProperty> askFor;

	@Builder
	@JsonCreator
	public MusicIdentificationQuestion(
			@JsonProperty("points") long points,
			@JsonProperty("track_path") String trackPath,
			@JsonProperty("ask_for") List<TrackProperty> askFor) {
		super(points);
		
		Preconditions.checkArgument(!StringUtils.isBlank(trackPath), "trackPath must not be blank.");
		this.trackPath = trackPath;

		Preconditions.checkNotNull(askFor, "askFor must be non-null.");
		this.askFor = Sets.immutableEnumSet(askFor);
		Preconditions.checkArgument(!this.askFor.isEmpty(), "askFor must not be empty.");
	}
}
