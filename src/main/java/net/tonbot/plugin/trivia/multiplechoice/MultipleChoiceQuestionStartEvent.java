package net.tonbot.plugin.trivia.multiplechoice;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.QuestionStartEvent;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultipleChoiceQuestionStartEvent extends QuestionStartEvent<MultipleChoiceQuestion> {

	/**
	 * @param question
	 *            {@link MultipleChoiceQuestion}. Non-null.
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param maxDurationMs
	 *            The maximum time to wait for a correct answer. Must be positive.
	 */
	@Builder
	private MultipleChoiceQuestionStartEvent(MultipleChoiceQuestion question, long questionNumber,
			long totalQuestions, long maxDurationMs) {
		super(question, questionNumber, totalQuestions, maxDurationMs);
	}
}
