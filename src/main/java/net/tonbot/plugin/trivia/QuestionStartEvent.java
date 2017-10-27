package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Data;

@Data
abstract class QuestionStartEvent {

	private final long questionNumber;
	private final long totalQuestions;

	private final long points;

	private final long maxDurationSeconds;

	public QuestionStartEvent(long questionNumber, long totalQuestions, long points, long maxDurationSeconds) {
		Preconditions.checkArgument(questionNumber > 0, "questionNumber must be positive.");
		Preconditions.checkArgument(totalQuestions > 0, "totalQuestions must be positive.");
		Preconditions.checkArgument(points >= 0, "points must be non-negative.");
		Preconditions.checkArgument(maxDurationSeconds > 0, "maxDurationSeconds must be positive.");

		this.questionNumber = questionNumber;
		this.totalQuestions = totalQuestions;
		this.points = points;
		this.maxDurationSeconds = maxDurationSeconds;
	}
}
