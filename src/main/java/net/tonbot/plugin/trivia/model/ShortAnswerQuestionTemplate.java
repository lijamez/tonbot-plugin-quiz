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
public class ShortAnswerQuestionTemplate extends QuestionTemplate {

	private final String question;
	private final List<String> answers;

	/**
	 * The constructor for a short answer question template.
	 * @param points The number of points this question is worth.
	 * @param difficulty The difficulty. 0 is the easiest while 1 is the hardest. Nullable. 
	 * @param imagePaths Path to images. Non-null.
	 * @param question The question. Non-null.
	 * @param answers A list of possible answers. Non-null, non-empty.
	 */
	@Builder
	@JsonCreator
	public ShortAnswerQuestionTemplate(
			@JsonProperty("points") long points,
			@JsonProperty("difficulty") Double difficulty,
			@JsonProperty("images") List<String> imagePaths, 
			@JsonProperty("question") String question,
			@JsonProperty("answers") List<String> answers) {
		super(points, difficulty, imagePaths);

		Preconditions.checkArgument(!StringUtils.isBlank(question), "question must not be blank.");
		this.question = question;

		Preconditions.checkNotNull(answers, "answers must be non-null.");
		this.answers = ImmutableList.copyOf(answers);
		Preconditions.checkArgument(!CollectionUtils.isEmpty(this.answers), "answers must not be empty.");
		this.answers.forEach(
				a -> Preconditions.checkArgument(!StringUtils.isBlank(a), "answers must not contain a blank answer."));
	}
}
