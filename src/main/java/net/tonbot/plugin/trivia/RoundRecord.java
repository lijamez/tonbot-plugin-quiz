package net.tonbot.plugin.trivia;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Data;

@Data
public class RoundRecord {

	@JsonProperty("questionRecords")
	private final List<QuestionRecord> questionRecords;
	
	public RoundRecord() {
		this.questionRecords = new ArrayList<>();
	}
	
	@JsonCreator
	RoundRecord(@JsonProperty("questionRecords") List<QuestionRecord> questionRecords) {
		this.questionRecords = Preconditions.checkNotNull(questionRecords);
	}
	
	public long getTotalEarnedScore() {
		return questionRecords.stream()
			.mapToLong(qr -> qr.getEarnedPoints())
			.sum();
	}
	
	public long getTotalPossibleScore() {
		return questionRecords.stream()
				.mapToLong(qr -> qr.getValue())
				.sum();
	}
	
	public long getTotalQuestions() {
		return questionRecords.size();
	}
	
	public long getTotalCorrectlyAnsweredQuestions() {
		return questionRecords.stream()
				.filter(qr -> qr.isAnsweredCorrectly())
				.count();
	}
}
