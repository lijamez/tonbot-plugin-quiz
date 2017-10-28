package net.tonbot.plugin.trivia.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Choice {

	private final String value;
	private final boolean isCorrect;

	@Builder
	@JsonCreator
	public Choice(
			@JsonProperty("value") String value,
			@JsonProperty("isCorrect") boolean isCorrect) {
		Preconditions.checkArgument(!StringUtils.isBlank(value), "value must not be blank.");
		this.value = value;
		this.isCorrect = isCorrect;
	}
}
