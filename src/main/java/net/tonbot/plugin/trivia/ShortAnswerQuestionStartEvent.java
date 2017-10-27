package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class ShortAnswerQuestionStartEvent extends QuestionStartEvent {

	private String question;

	/**
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param points
	 *            The number points that this question is worth.
	 * @param maxDurationSeconds
	 *            The maximum amount of time to wait for a correct answer.
	 * @param question
	 *            The question to ask. Non-null.
	 */
	@Builder
	public ShortAnswerQuestionStartEvent(
			long questionNumber,
			long totalQuestions,
			long points,
			long maxDurationSeconds,
			String question) {
		super(questionNumber, totalQuestions, points, maxDurationSeconds);

		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
	}
}
