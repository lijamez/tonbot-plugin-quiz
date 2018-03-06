package net.tonbot.plugin.trivia.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Data;
import net.tonbot.plugin.trivia.musicid.SongProperty;

@Data
public class SongPropertyWeight {

	private final SongProperty property;
	private final long weight;
	
	@JsonCreator
	public SongPropertyWeight(
			@JsonProperty("property") SongProperty property, 
			@JsonProperty("weight") Long weight) {
		this.property = Preconditions.checkNotNull(property, "property must be non-null.");
		this.weight = Preconditions.checkNotNull(weight, "weight must be non-null.");
		Preconditions.checkArgument(weight >= 0, "weight must be non-negative.");
	}
}
