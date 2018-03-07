package net.tonbot.plugin.trivia.shortanswer;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.QuestionEndEvent;
import net.tonbot.plugin.trivia.Win;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShortAnswerQuestionEndEvent extends QuestionEndEvent<ShortAnswerQuestion> {

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
	public ShortAnswerQuestionEndEvent(ShortAnswerQuestion question, boolean timedOut, Win win, String acceptableAnswer) {
		super(question, timedOut, win);

		this.acceptableAnswer = Preconditions.checkNotNull(acceptableAnswer, "acceptableAnswer must be non-null.");
	}
}
