package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Data;
import net.tonbot.plugin.trivia.model.Question;

@Data
abstract class QuestionStartEvent {

	private final Question question;

	private final long questionNumber;
	private final long totalQuestions;

	private final long maxDurationSeconds;

	public QuestionStartEvent(Question question, long questionNumber, long totalQuestions, long maxDurationSeconds) {
		Preconditions.checkArgument(questionNumber > 0, "questionNumber must be positive.");
		Preconditions.checkArgument(totalQuestions > 0, "totalQuestions must be positive.");
		Preconditions.checkArgument(maxDurationSeconds > 0, "maxDurationSeconds must be positive.");

		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.questionNumber = questionNumber;
		this.totalQuestions = totalQuestions;
		this.maxDurationSeconds = maxDurationSeconds;
	}
}
