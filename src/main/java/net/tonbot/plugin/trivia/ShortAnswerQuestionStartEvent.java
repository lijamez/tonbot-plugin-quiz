package net.tonbot.plugin.trivia;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestion;

@Data
@EqualsAndHashCode(callSuper = true)
class ShortAnswerQuestionStartEvent extends QuestionStartEvent {

	/**
	 * @param question
	 *            The {@link ShortAnswerQuestion}. Non-null.
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param maxDurationSeconds
	 *            The maximum amount of time to wait for a correct answer.
	 */
	@Builder
	public ShortAnswerQuestionStartEvent(
			ShortAnswerQuestion saQuestion,
			long questionNumber,
			long totalQuestions,
			long maxDurationSeconds) {
		super(saQuestion, questionNumber, totalQuestions, maxDurationSeconds);
	}

	public ShortAnswerQuestion getShortAnswerQuestion() {
		return (ShortAnswerQuestion) this.getQuestion();
	}
}
