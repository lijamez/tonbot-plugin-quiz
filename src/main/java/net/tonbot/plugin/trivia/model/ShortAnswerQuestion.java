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
public class ShortAnswerQuestion extends Question {

	private final String question;
	private final List<String> answers;

	@Builder
	@JsonCreator
	public ShortAnswerQuestion(
			@JsonProperty("points") long points,
			@JsonProperty("image") String imageUrl,
			@JsonProperty("question") String question,
			@JsonProperty("answers") List<String> answers) {
		super(points, imageUrl);

		Preconditions.checkArgument(!StringUtils.isBlank(question), "question must not be blank.");
		this.question = question;

		Preconditions.checkNotNull(answers, "answers must be non-null.");
		this.answers = ImmutableList.copyOf(answers);
		Preconditions.checkArgument(!CollectionUtils.isEmpty(this.answers), "answers must not be empty.");
		this.answers.forEach(
				a -> Preconditions.checkArgument(!StringUtils.isBlank(a), "answers must not contain a blank answer."));
	}
}
