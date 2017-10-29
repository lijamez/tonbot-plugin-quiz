package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class MusicIdQuestionEndEvent extends QuestionEndEvent {

	private final String canonicalAnswer;

	/**
	 * 
	 * @param timedOut
	 *            Whether if the question timed out or not.
	 * @param win
	 *            The win details, if any. Nullable.
	 * @param canonicalAnswer
	 *            The "canonical" answer. Non-null.
	 * @param answererId
	 *            The user who answered correctly, if any. Nullable.
	 */
	@Builder
	private MusicIdQuestionEndEvent(
			boolean timedOut,
			Win win,
			String canonicalAnswer) {
		super(timedOut, win);

		this.canonicalAnswer = Preconditions.checkNotNull(canonicalAnswer, "canonicalAnswer must be non-null.");
	}
}
