package net.tonbot.plugin.trivia.model;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultipleChoiceQuestionTemplate extends QuestionTemplate {

	private final String question;
	private final List<Choice> correctChoices;
	private final List<Choice> incorrectChoices;

	@Builder
	@JsonCreator
	public MultipleChoiceQuestionTemplate(
			@JsonProperty("points") long points,
			@JsonProperty("images") List<String> imagePaths,
			@JsonProperty("question") String question,
			@JsonProperty("choices") List<Choice> choices) {
		super(points, imagePaths);

		Preconditions.checkArgument(!StringUtils.isBlank(question), "question must not be blank.");
		this.question = question;

		Preconditions.checkArgument(!CollectionUtils.isEmpty(choices), "choices must not be empty.");

		this.correctChoices = choices.stream()
				.filter(c -> c.isCorrect())
				.collect(ImmutableList.toImmutableList());

		this.incorrectChoices = choices.stream()
				.filter(c -> !c.isCorrect())
				.collect(ImmutableList.toImmutableList());

		Preconditions.checkArgument(!correctChoices.isEmpty() && !incorrectChoices.isEmpty(),
				"choices must contain at least one correct choice and at least one incorrect choice.");
	}
}
