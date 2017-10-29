package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class ShortAnswerQuestionEndEvent extends QuestionEndEvent {

	private final String acceptableAnswer;

	/**
	 * 
	 * @param timedOut
	 *            Whether if the question ended due to a timeout.
	 * @param win
	 *            Details of a win, if any. Nullable.
	 * @param acceptableAnswer
	 *            An acceptable answer. Non-null.
	 */
	@Builder
	public ShortAnswerQuestionEndEvent(
			boolean timedOut,
			Win win,
			String acceptableAnswer) {
		super(timedOut, win);

		this.acceptableAnswer = Preconditions.checkNotNull(acceptableAnswer, "acceptableAnswer must be non-null.");
	}
}
