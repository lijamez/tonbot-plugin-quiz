package net.tonbot.plugin.quiz.model;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;

@Data
public class QuizMetadata {

	private final String name;
	private final String version;
	private final String description;

	@Builder
	@JsonCreator
	public QuizMetadata(
			@JsonProperty("name") String name,
			@JsonProperty("version") String version,
			@JsonProperty("description") String description) {
		Preconditions.checkArgument(!StringUtils.isBlank(name), "name must not be blank.");
		Preconditions.checkArgument(!StringUtils.isBlank(version), "version must not be blank.");
		Preconditions.checkArgument(!StringUtils.isBlank(description), "description must not be blank.");

		this.name = name;
		this.version = version;
		this.description = description;
	}

}
