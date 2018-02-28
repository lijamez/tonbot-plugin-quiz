package net.tonbot.plugin.trivia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

class Scorekeeper {

	// A map of scorekeeping records up to but not including the current question.
	private final Map<Long, Record> overallRecords;
	
	// The map of QuestionRecords for the current question that are being updated as users attempt to answer it.
	// Invariant: Keyset is always a superset of overallRecords' keyset.
	private final Map<Long, QuestionRecord> currentQuestionRecords;
	
	private final double scoreDecayFactor;

	private Long currentQuestionPointValue;
	
	// Contains a history of reference QuestionRecords for question that were already completed.
	// Because participants can join the trivia at any time, the scorekeeper may need to backfill
	// records that were missed. It will backfill them with question records from this collection.
	private List<QuestionRecord> questionRecordHistory;
	
	// The current reference question record.
	private QuestionRecord currentReferenceQuestionRecord;

	public Scorekeeper(double scoreDecayFactor) {
		this.overallRecords = new HashMap<>();
		this.currentQuestionRecords = new HashMap<>();
		this.scoreDecayFactor = scoreDecayFactor;
		this.questionRecordHistory = new ArrayList<>();
	}

	/**
	 * Sets up a question with the given amount of points.
	 * 
	 * @param points
	 *            The number of points. Must be non-negative.
	 */
	public void setupQuestion(long points) {
		Preconditions.checkNotNull(points >= 0, "points must be non-negative.");

		this.currentQuestionPointValue = points;
		this.currentQuestionRecords.clear();
		
		this.currentReferenceQuestionRecord = new QuestionRecord(points, scoreDecayFactor);
		
		// Ensures that currentReferenceQuestionRecord's keyset is always a superset of overallRecords' keyset.
		for (Long userId : overallRecords.keySet()) {
			this.currentQuestionRecords.put(userId, this.currentReferenceQuestionRecord.clone());
		}
	}
	
	/**
	 * Ends the current question. No-op if there is currently no active question.
	 */
	public void endQuestion() {
		if (this.currentQuestionPointValue == null) {
			return;
		}
		
		// State change - clears the current question
		mergeCurrentRecordsIntoOverallRecords();
		this.questionRecordHistory.add(currentReferenceQuestionRecord);
		this.currentReferenceQuestionRecord = null;
		this.currentQuestionPointValue = null;
	}

	/**
	 * Logs that the user has supplied a correct answer.
	 * 
	 * @param userId
	 *            The ID of the user that provided the correct answer.
	 * @return The actual number of points awarded.
	 * @throws IllegalStateException
	 *             if a question has not yet been set up.
	 */
	public long logCorrectAnswer(long userId) {
		Preconditions.checkState(this.currentQuestionPointValue != null, "Question has not yet been set up.");

		QuestionRecord questionRecord = this.currentQuestionRecords
				.computeIfAbsent(userId, i -> currentReferenceQuestionRecord.clone());

		questionRecord.setAnsweredCorrectly(true);
		
		return questionRecord.getEarnedPoints();
	}
	
	private void mergeCurrentRecordsIntoOverallRecords() {
		
		// This is safe, since currentQuestionRecords keyset is a superset of overallRecords' keyset.
		for (Entry<Long, QuestionRecord> currentQuestionRecordEntry : currentQuestionRecords.entrySet()) {
			long uid = currentQuestionRecordEntry.getKey();
			
			Record record = overallRecords.get(uid);
			if (record == null) {
				// New user. Must backfill all question records.
				record = new Record();
				record.getQuestionRecords().addAll(questionRecordHistory);
				
				overallRecords.put(uid, record);
			}
			
			record.getQuestionRecords().add(currentQuestionRecordEntry.getValue());
		}
		
		this.currentQuestionRecords.clear();
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
		Preconditions.checkState(this.currentQuestionPointValue != null, "Question has not yet been set up.");
		
		QuestionRecord questionRecord = this.currentQuestionRecords
				.computeIfAbsent(userId, i -> currentReferenceQuestionRecord.clone());

		questionRecord.setIncorrectAnswers(questionRecord.getIncorrectAnswers() + 1);
	}

	/**
	 * Gets the {@link QuestionRecord} for the current question and given
	 * user ID.
	 * 
	 * @param userId
	 *            User ID.
	 * @returns The {@link QuestionRecord} for that user.
	 */
	public QuestionRecord getQuestionRecord(long userId) {
		QuestionRecord questionRecord = this.currentQuestionRecords
				.computeIfAbsent(userId, i -> currentReferenceQuestionRecord.clone());
		
		return questionRecord;
	}

	/**
	 * Gets an immutable map of the overall records.
	 * 
	 * @return An immutable map a map of user IDs to all activity logged for that user.
	 */
	public Map<Long, Record> getRecords() {
		return ImmutableMap.copyOf(overallRecords);
	}
}
