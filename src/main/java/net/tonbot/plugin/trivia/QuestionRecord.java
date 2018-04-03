package net.tonbot.plugin.trivia;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
class QuestionRecord {

	// The number of points that this question is worth normally.
	@JsonProperty("value")
	private final long value;
	
	@JsonProperty("scoreDecayFactor")
	private final double scoreDecayFactor;
	
	@JsonProperty("answeredCorrectly")
	private boolean answeredCorrectly = false;
	
	@JsonProperty("incorrectAnswers")
	private long incorrectAnswers = 0;
	
	@JsonProperty("timeToAnswerMs")
	private Long timeToAnswerMs; 
	
	public QuestionRecord(long value, double scoreDecayFactor) {
		this.value = value;
		this.scoreDecayFactor = scoreDecayFactor;
	}
	
	@JsonCreator
	QuestionRecord(
			@JsonProperty("value") long value,
			@JsonProperty("scoreDecayFactor") double scoreDecayFactor,
			@JsonProperty("answeredCorrectly") boolean answeredCorrectly,
			@JsonProperty("incorrectAnswers") long incorrectAnswers,
			@JsonProperty("timeToAnswerMs") Long timeToAnswerMs) {
		this.value = value;
		this.scoreDecayFactor = scoreDecayFactor;
		this.answeredCorrectly = answeredCorrectly;
		this.incorrectAnswers = incorrectAnswers;
		this.timeToAnswerMs = timeToAnswerMs;
	}
	
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
		QuestionRecord clonedRecord = new QuestionRecord(
				value, 
				scoreDecayFactor,
				answeredCorrectly,
				incorrectAnswers,
				timeToAnswerMs);
		
		return clonedRecord;
	}
}
