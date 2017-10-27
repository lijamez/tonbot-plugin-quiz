package net.tonbot.plugin.trivia;

import java.util.Optional;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class ShortAnswerQuestionEndEvent extends QuestionEndEvent {

	private final UserMessage correctUserResponse;
	private final String acceptableAnswer;

	/**
	 * 
	 * @param timedOut
	 *            Whether if the question ended due to a timeout.
	 * @param correctUserResponse
	 *            The correct user response, if any. Nullable.
	 * @param acceptableAnswer
	 *            An acceptable answer. Non-null.
	 */
	@Builder
	public ShortAnswerQuestionEndEvent(boolean timedOut, UserMessage correctUserResponse, String acceptableAnswer) {
		super(timedOut);

		this.correctUserResponse = correctUserResponse;
		this.acceptableAnswer = Preconditions.checkNotNull(acceptableAnswer, "acceptableAnswer must be non-null.");
	}

	public Optional<UserMessage> getCorrectUserResponse() {
		return Optional.ofNullable(correctUserResponse);
	}
}
