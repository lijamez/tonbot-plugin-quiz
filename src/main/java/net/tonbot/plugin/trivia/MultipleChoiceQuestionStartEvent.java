package net.tonbot.plugin.trivia;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestion;

@Data
@EqualsAndHashCode(callSuper = true)
class MultipleChoiceQuestionStartEvent extends QuestionStartEvent {

	private final List<Choice> choices;

	/**
	 * @param mcQuestion
	 *            {@link MultipleChoiceQuestion}. Non-null.
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param maxDurationSeconds
	 *            The maximum time to wait for a correct answer. Must be positive.
	 * @param choices
	 *            The choices for this particular ask of the question.
	 */
	@Builder
	private MultipleChoiceQuestionStartEvent(
			MultipleChoiceQuestion mcQuestion,
			long questionNumber,
			long totalQuestions,
			long maxDurationSeconds,
			List<Choice> choices) {
		super(mcQuestion, questionNumber, totalQuestions, maxDurationSeconds);

		Preconditions.checkNotNull(choices, "choices must be non-null.");
		this.choices = ImmutableList.copyOf(choices);
	}

	public MultipleChoiceQuestion getMultipleChoiceQuestion() {
		return (MultipleChoiceQuestion) this.getQuestion();
	}
}
