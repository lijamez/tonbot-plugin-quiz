package net.tonbot.plugin.quiz;

import com.google.common.base.Preconditions;

import lombok.Data;

@Data
abstract class QuestionStartEvent {

	private final long points;

	private final long maxDurationSeconds;

	public QuestionStartEvent(long points, long maxDurationSeconds) {
		Preconditions.checkArgument(points >= 0, "points must be non-negative.");
		Preconditions.checkArgument(maxDurationSeconds > 0, "maxDurationSeconds must be positive.");

		this.points = points;
		this.maxDurationSeconds = maxDurationSeconds;
	}
}
