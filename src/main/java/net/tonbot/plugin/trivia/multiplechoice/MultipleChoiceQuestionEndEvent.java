package net.tonbot.plugin.trivia.multiplechoice;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.QuestionEndEvent;
import net.tonbot.plugin.trivia.Win;
import net.tonbot.plugin.trivia.model.Choice;

@Data
@EqualsAndHashCode(callSuper = true)
public class MultipleChoiceQuestionEndEvent extends QuestionEndEvent<MultipleChoiceQuestion> {

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
	private MultipleChoiceQuestionEndEvent(MultipleChoiceQuestion question, boolean timedOut, Win win, Choice correctChoice) {
		super(question, timedOut, win);

		this.correctChoice = Preconditions.checkNotNull(correctChoice, "correctChoice must be non-null.");
	}

}
