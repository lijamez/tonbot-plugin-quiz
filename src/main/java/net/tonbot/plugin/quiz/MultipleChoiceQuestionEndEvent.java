package net.tonbot.plugin.quiz;

import java.util.Optional;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.quiz.model.Choice;

@Data
@EqualsAndHashCode(callSuper = true)
class MultipleChoiceQuestionEndEvent extends QuestionEndEvent {

	private final Choice correctChoice;
	private final Long answererId;
	
	/**
	 * 
	 * @param timedOut True iff the question was never answered.
	 * @param correctChoice The correct choice. Non-null.
	 * @param answererId The user who answered correctly, if any. Nullable.
	 */
	@Builder
	private MultipleChoiceQuestionEndEvent(boolean timedOut, Choice correctChoice, Long answererId) {
		super(timedOut);
		
		this.correctChoice = Preconditions.checkNotNull(correctChoice, "correctChoice must be non-null.");
		this.answererId = answererId;
	}
	
	/**
	 *  
	 * @return The user ID of the user who answered correctly. Empty if no one answered correctly.
	 */
	public Optional<Long> getAnswererId() {
		return Optional.ofNullable(answererId);
	}
}
