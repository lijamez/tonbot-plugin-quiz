package net.tonbot.plugin.trivia;

import java.io.File;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;

@Data
@EqualsAndHashCode(callSuper = true)
class ShortAnswerQuestionStartEvent extends QuestionStartEvent {

	/**
	 * @param question
	 *            The {@link ShortAnswerQuestionTemplate}. Non-null.
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param maxDurationSeconds
	 *            The maximum amount of time to wait for a correct answer.
	 * @param imageFile
	 *            Image file. Nullable.
	 */
	@Builder
	public ShortAnswerQuestionStartEvent(ShortAnswerQuestionTemplate saQuestion, long questionNumber,
			long totalQuestions, long maxDurationSeconds, File imageFile) {
		super(saQuestion, questionNumber, totalQuestions, maxDurationSeconds, imageFile);
	}

	public ShortAnswerQuestionTemplate getShortAnswerQuestion() {
		return (ShortAnswerQuestionTemplate) this.getQuestion();
	}
}
