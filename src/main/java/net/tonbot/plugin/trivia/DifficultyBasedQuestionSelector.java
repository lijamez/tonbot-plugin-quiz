package net.tonbot.plugin.trivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.QuestionTemplate;

class DifficultyBasedQuestionSelector {
	
	private static final double STANDARD_DEVIATION = 0.4;
	
	private final Random random;
	
	@Inject
	public DifficultyBasedQuestionSelector(Random random) {
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
	}

	/**
	 * Picks questions from a set with a bias towards those with a difficulty closer
	 * to the given discrete {@link Difficulty}.
	 * 
	 * @param fullList
	 *            The full list of {@link QuestionTemplate}s. Non-null.
	 * @param difficulty
	 *            The {@link Difficulty} to have a bias towards.
	 * @return A list of selected {@link QuestionTemplate}
	 */
	public List<QuestionTemplate> pick(List<QuestionTemplate> fullList, Difficulty difficulty, int limit) {
		Preconditions.checkNotNull(fullList, "fullList must be non-null.");
		Preconditions.checkNotNull(difficulty, "difficulty must be non-null.");
		Preconditions.checkArgument(limit >= 0, "limit must be a positive number.");
		
		List<QuestionTemplate> result = new ArrayList<>();
		
		if (fullList.size() <= limit) {
			result.addAll(fullList);
		} else {
			// Some question templates don't have difficulties. Find the ratio of QTs with and without difficulties
			// and we will use this ratio to select questions randomly and normally.
			List<QuestionTemplate> questionsWithDifficulty = fullList.stream()
					.filter(qt -> qt.getDifficulty().isPresent())
					.collect(Collectors.toList());
			List<QuestionTemplate> questionsWithoutDifficulty = fullList.stream()
					.filter(qt -> qt.getDifficulty().isPresent())
					.collect(Collectors.toList());
			
			double ratio = questionsWithDifficulty.size() / (double) fullList.size();
			
			int numberOfQuestionsWithDifficultyNeeded = (int) (limit * ratio);
			int numberOfQuesionsWithoutDifficultyNeeded = limit - numberOfQuestionsWithDifficultyNeeded;
			
			result.addAll(pickQuestionsWithDifficulty(questionsWithDifficulty, difficulty, numberOfQuestionsWithDifficultyNeeded));
			result.addAll(pickQuestionsWithoutDifficulty(questionsWithoutDifficulty, numberOfQuesionsWithoutDifficultyNeeded));
		}
		
		Collections.shuffle(result);
		
		return result;
	}
	
	private List<QuestionTemplate> pickQuestionsWithDifficulty(List<QuestionTemplate> qtsWithDifficulty, Difficulty difficulty, int limit) {
		Preconditions.checkArgument(limit <= qtsWithDifficulty.size(), "limit must be less than qtsWithDifficulty cardinality");
		
		Difficulty nextHigherDifficulty = difficulty.getNextDifficulty();
		
		double mean = (difficulty.getMinQuestionDifficulty() + nextHigherDifficulty.getMinQuestionDifficulty()) / 2;
		
		List<QuestionTemplate> remainingQuestions = new ArrayList<>(qtsWithDifficulty);
		List<QuestionTemplate> selectedQuestions = new ArrayList<>();
		
		for (int i = 0; i < limit; i++) {
			double targetDifficulty = random.nextGaussian() * STANDARD_DEVIATION + mean;
			
			QuestionTemplate selectedQuestion = remainingQuestions.stream()
				.min((a, b) -> (int) (Math.abs(a.getDifficulty().get() - targetDifficulty) - Math.abs(b.getDifficulty().get() - targetDifficulty)))
				.get();
			
			selectedQuestions.add(selectedQuestion);
			remainingQuestions.remove(selectedQuestion);
		}
		
		return selectedQuestions;
	}
	
	private List<QuestionTemplate> pickQuestionsWithoutDifficulty(List<QuestionTemplate> qts, int limit) {
		Preconditions.checkArgument(limit <= qts.size(), "limit must be less than qts cardinality");
				
		Collections.shuffle(qts);
		return qts.subList(0, limit);
	}
}
