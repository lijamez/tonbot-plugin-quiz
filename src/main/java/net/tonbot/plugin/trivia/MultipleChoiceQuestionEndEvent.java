package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.Choice;

@Data
@EqualsAndHashCode(callSuper = true)
class MultipleChoiceQuestionEndEvent extends QuestionEndEvent {

	private final Choice correctChoice;

	/**
	 * 
	 * @param timedOut
	 *            True iff the question was never answered.
	 * @param win
	 *            The win details, if any. Nullable.
	 * @param correctChoice
	 *            The correct choice. Non-null.
	 * 
	 */
	@Builder
	private MultipleChoiceQuestionEndEvent(boolean timedOut, Win win, Choice correctChoice) {
		super(timedOut, win);

		this.correctChoice = Preconditions.checkNotNull(correctChoice, "correctChoice must be non-null.");
	}

}
