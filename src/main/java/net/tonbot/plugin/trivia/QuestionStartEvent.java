package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Data;
import net.tonbot.plugin.trivia.model.Question;

@Data
public abstract class QuestionStartEvent<T extends Question> {

	private final T question;

	private final long questionNumber;
	private final long totalQuestions;

	private final long maxDurationMs;

	public QuestionStartEvent(T question, long questionNumber, long totalQuestions,
			long maxDurationMs) {
		Preconditions.checkArgument(questionNumber > 0, "questionNumber must be positive.");
		Preconditions.checkArgument(totalQuestions > 0, "totalQuestions must be positive.");
		Preconditions.checkArgument(maxDurationMs > 0, "maxDurationMs must be positive.");

		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.questionNumber = questionNumber;
		this.totalQuestions = totalQuestions;
		this.maxDurationMs = maxDurationMs;
	}
}
