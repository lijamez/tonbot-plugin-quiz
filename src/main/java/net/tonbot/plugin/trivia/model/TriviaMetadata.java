package net.tonbot.plugin.trivia.model;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;

@Data
public class TriviaMetadata {

	private final String name;
	private final String version;
	private final String description;
	private final int defaultQuestionsPerRound;
	private final long defaultTimePerQuestion; //in ms
	private final List<List<String>> synonyms;

	@Builder
	@JsonCreator
	public TriviaMetadata(
			@JsonProperty("name") String name, 
			@JsonProperty("version") String version,
			@JsonProperty("description") String description,
			@JsonProperty("defaultQuestionsPerRound") int defaultQuestionsPerRound,
			@JsonProperty("defaultTimePerQuestion") long defaultTimePerQuestion,
			@JsonProperty("synonyms") List<List<String>> synonyms) {
		Preconditions.checkArgument(!StringUtils.isBlank(name), "name must not be blank.");
		Preconditions.checkArgument(!StringUtils.isBlank(version), "version must not be blank.");
		Preconditions.checkArgument(!StringUtils.isBlank(description), "description must not be blank.");
		Preconditions.checkArgument(defaultQuestionsPerRound > 0, "defaultQuestionsPerRound must be greater than 0.");
		Preconditions.checkArgument(defaultTimePerQuestion > 0, "defaultTimePerQuestion must be greater than 0.");
		
		this.name = name;
		this.version = version;
		this.description = description;
		this.defaultQuestionsPerRound = defaultQuestionsPerRound;
		this.defaultTimePerQuestion = defaultTimePerQuestion;
		this.synonyms = synonyms == null ? ImmutableList.of() : ImmutableList.copyOf(synonyms);
	}

}
