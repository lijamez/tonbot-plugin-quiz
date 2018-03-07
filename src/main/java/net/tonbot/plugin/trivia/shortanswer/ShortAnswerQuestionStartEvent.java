package net.tonbot.plugin.trivia.shortanswer;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.QuestionStartEvent;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShortAnswerQuestionStartEvent extends QuestionStartEvent<ShortAnswerQuestion> {

	/**
	 * @param saQuestion
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
			ShortAnswerQuestion question, 
			long questionNumber,
			long totalQuestions, 
			long maxDurationSeconds) {
		super(question, questionNumber, totalQuestions, maxDurationSeconds);
	}
}
