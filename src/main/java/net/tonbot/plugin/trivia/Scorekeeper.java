package net.tonbot.plugin.trivia;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

class Scorekeeper {

	private final Map<Long, Long> scores;
	private final Map<Long, Long> attempts;

	private Long currentQuestionPoints;

	public Scorekeeper() {
		this.scores = new HashMap<>();
		this.attempts = new HashMap<>();
	}

	/**
	 * Sets up a question with the given amount of points.
	 * 
	 * @param points
	 *            The number of points. Must be non-negative.
	 */
	public void setupQuestion(long points) {
		Preconditions.checkNotNull(points >= 0, "points must be non-negative.");

		this.currentQuestionPoints = points;
		this.attempts.clear();
	}

	/**
	 * Logs that the user has supplied a correct answer and then clears the current
	 * question.
	 * 
	 * @param userId
	 *            The ID of the user that provided the correct answer.
	 * @return The actual number of points awarded.
	 * @throws IllegalStateException
	 *             if a question has not yet been set up.
	 */
	public long logCorrectAnswerAndAdvance(long userId) {
		if (this.currentQuestionPoints == null) {
			throw new IllegalStateException("Question has not yet been set up.");
		}

		long previousIncorrectAnswers = this.attempts.getOrDefault(userId, 0L);
		long actualAwardedPoints = (long) Math.ceil(currentQuestionPoints * Math.pow(0.5, previousIncorrectAnswers));

		long oldScore = this.scores.getOrDefault(userId, 0L);
		this.scores.put(userId, oldScore + actualAwardedPoints);

		this.currentQuestionPoints = null;
		this.attempts.clear();

		return actualAwardedPoints;
	}

	/**
	 * Logs that the user has supplied an incorrect answer.
	 * 
	 * @param userId
	 *            The user ID.
	 * @throws IllegalStateException
	 *             if a question has not yet been set up.
	 */
	public void logIncorrectAnswer(long userId) {
		if (this.currentQuestionPoints == null) {
			throw new IllegalStateException("Question has not yet been set up.");
		}

		long oldAttemptsCount = this.attempts.getOrDefault(userId, 0L);
		this.attempts.put(userId, oldAttemptsCount + 1);

		// Participating means the user gets mentioned in the scoreboard.
		this.scores.putIfAbsent(userId, 0L);
	}

	/**
	 * Gets the number of incorrect answers for the current question for the given
	 * user ID.
	 * 
	 * @param userId
	 *            User ID.
	 */
	public long getIncorrectAnswers(long userId) {
		return this.attempts.getOrDefault(userId, 0L);
	}

	/**
	 * Gets an immutable map of the scores.
	 * 
	 * @return An immutable map from user ID to score.
	 */
	public Map<Long, Long> getScores() {
		return ImmutableMap.copyOf(scores);
	}
}
