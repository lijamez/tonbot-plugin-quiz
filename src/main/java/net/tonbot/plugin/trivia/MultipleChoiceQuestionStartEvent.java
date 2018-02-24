package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
class MultipleChoiceQuestionStartEvent extends QuestionStartEvent {

	private final List<Choice> choices;

	/**
	 * @param mcQuestion
	 *            {@link MultipleChoiceQuestionTemplate}. Non-null.
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param maxDurationMs
	 *            The maximum time to wait for a correct answer. Must be positive.
	 * @param choices
	 *            The choices for this particular ask of the question.
	 * @param imageFile
	 *            Image file. Nullable.
	 */
	@Builder
	private MultipleChoiceQuestionStartEvent(MultipleChoiceQuestionTemplate mcQuestion, long questionNumber,
			long totalQuestions, long maxDurationMs, List<Choice> choices, File imageFile) {
		super(mcQuestion, questionNumber, totalQuestions, maxDurationMs, imageFile);

		Preconditions.checkNotNull(choices, "choices must be non-null.");
		this.choices = ImmutableList.copyOf(choices);
	}

	public MultipleChoiceQuestionTemplate getMultipleChoiceQuestion() {
		return (MultipleChoiceQuestionTemplate) this.getQuestion();
	}
}
