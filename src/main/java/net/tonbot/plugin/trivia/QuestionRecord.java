package net.tonbot.plugin.trivia;

import lombok.Data;

@Data
class QuestionRecord {

	// The number of points that this question is worth normally.
	private final long value;
	private final double scoreDecayFactor;
	
	private boolean answeredCorrectly = false;
	private long incorrectAnswers = 0;
	
	public long getEarnedPoints() {
		if (!answeredCorrectly) {
			return 0;
		}
		
		long earnedPoints = (long) Math.ceil(value * Math.pow(scoreDecayFactor, incorrectAnswers));
		return earnedPoints;
	}
	
	/**
	 * Creates a deep clone of this object.
	 * @return A deep clone of this object.
	 */
	public QuestionRecord clone() {
		QuestionRecord clonedRecord = new QuestionRecord(value, scoreDecayFactor);
		clonedRecord.setAnsweredCorrectly(answeredCorrectly);
		clonedRecord.setIncorrectAnswers(incorrectAnswers);
		
		return clonedRecord;
	}
}
