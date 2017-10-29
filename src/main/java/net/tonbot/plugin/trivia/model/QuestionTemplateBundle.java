package net.tonbot.plugin.trivia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;

@Data
public class QuestionTemplateBundle {

	private final List<QuestionTemplate> questionTemplates;

	@Builder
	@JsonCreator
	public QuestionTemplateBundle(
			@JsonProperty("questions") List<QuestionTemplate> questionTemplates) {
		Preconditions.checkNotNull(questionTemplates, "questionTemplates must be non-null.");
		this.questionTemplates = ImmutableList.copyOf(questionTemplates);
	}
}
