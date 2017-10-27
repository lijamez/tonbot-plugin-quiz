package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class MusicIdQuestionEndEvent extends QuestionEndEvent {

	private final String canonicalAnswer;
	private final long answererId;

	/**
	 * 
	 * @param timedOut
	 *            Whether if the question timed out or not.
	 * @param canonicalAnswer
	 *            The "canonical" answer. Non-null.
	 * @param answererId
	 *            The user who answered correctly, if any. Nullable.
	 */
	@Builder
	private MusicIdQuestionEndEvent(boolean timedOut, String canonicalAnswer, Long answererId) {
		super(timedOut);

		this.canonicalAnswer = Preconditions.checkNotNull(canonicalAnswer, "canonicalAnswer must be non-null.");
		this.answererId = answererId;
	}
}
