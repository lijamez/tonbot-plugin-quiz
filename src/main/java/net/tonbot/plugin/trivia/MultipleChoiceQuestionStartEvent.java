package net.tonbot.plugin.trivia;

import java.util.List;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.Choice;

@Data
@EqualsAndHashCode(callSuper = true)
class MultipleChoiceQuestionStartEvent extends QuestionStartEvent {

	private final String question;

	private final List<Choice> choices;

	/**
	 * 
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param points
	 *            The number of points that this question is worth. Must be
	 *            non-negative.
	 * @param maxDurationSeconds
	 *            The maximum time to wait for a correct answer. Must be positive.
	 * @param question
	 *            The question to ask. Non-null.
	 * @param choices
	 *            The choices. They should be displayed to users in the same order
	 *            listed. Non-null.
	 */
	@Builder
	private MultipleChoiceQuestionStartEvent(
			long questionNumber,
			long totalQuestions,
			long points,
			long maxDurationSeconds,
			String question,
			List<Choice> choices) {
		super(questionNumber, totalQuestions, points, maxDurationSeconds);

		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.choices = Preconditions.checkNotNull(choices, "choices must be non-null.");
	}
}
