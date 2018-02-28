package net.tonbot.plugin.trivia;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
class Record {

	private final List<QuestionRecord> questionRecords;
	
	public Record() {
		this.questionRecords = new ArrayList<>();
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
