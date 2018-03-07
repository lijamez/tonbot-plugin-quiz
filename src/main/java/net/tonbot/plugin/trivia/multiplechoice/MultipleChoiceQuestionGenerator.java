package net.tonbot.plugin.trivia.multiplechoice;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.LoadedTrivia;
import net.tonbot.plugin.trivia.QuestionGenerator;
import net.tonbot.plugin.trivia.TriviaConfiguration;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate;

public class MultipleChoiceQuestionGenerator implements QuestionGenerator<MultipleChoiceQuestionTemplate, MultipleChoiceQuestion> {

	private final Random random;
	
	@Inject
	public MultipleChoiceQuestionGenerator(Random random) {
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
	}
	
	@Override
	public MultipleChoiceQuestion generate(LoadedTrivia loadedTrivia, TriviaConfiguration config, MultipleChoiceQuestionTemplate questionTemplate) {
		Preconditions.checkNotNull(loadedTrivia, "loadedTrivia must be non-null.");
		Preconditions.checkNotNull(questionTemplate, "questionTemplate must be non-null.");
		
		File imageFile = getRandomImageFile(questionTemplate, loadedTrivia.getTriviaTopicDir());
		
		List<Choice> choices = getChoices(questionTemplate, config.getMaxMultipleChoices());
		
		return MultipleChoiceQuestion.builder()
			.query(questionTemplate.getQuestion())
			.choices(choices)
			.image(imageFile)
			.points(questionTemplate.getPoints())
			.build();
	}
	
	/**
	 * Selects a list of choices. Only 1 of those choices are correct.
	 * 
	 * @param question
	 *            A {@link MultipleChoiceQuestionTemplate}.
	 * @param maxChoices
	 *            The maximum number of choices.
	 * @return A list of choices, only 1 of which are correct.
	 */
	private List<Choice> getChoices(MultipleChoiceQuestionTemplate questionTemplate, int maxChoices) {
		List<Choice> incorrectChoices = pickRandomSubset(questionTemplate.getIncorrectChoices(), maxChoices - 1);
		List<Choice> correctChoices = pickRandomSubset(questionTemplate.getCorrectChoices(), 1);

		List<Choice> allChoices = new ArrayList<>();
		allChoices.addAll(incorrectChoices);
		allChoices.addAll(correctChoices);
		Collections.shuffle(allChoices);

		return allChoices;
	}
	
	private List<Choice> pickRandomSubset(List<Choice> list, int num) {
		if (list.isEmpty()) {
			return list;
		} else {
			List<Choice> randomized = new ArrayList<>();
			randomized.addAll(list);
			Collections.shuffle(randomized);
			return randomized.subList(0, Math.min(randomized.size(), num));
		}
	}
	
	private File getRandomImageFile(MultipleChoiceQuestionTemplate q, File triviaRoot) {
		File imageFile = null;
		if (!q.getImagePaths().isEmpty()) {
			String imagePath = q.getImagePaths().get(random.nextInt(q.getImagePaths().size()));
			imageFile = new File(triviaRoot, imagePath);
		}

		return imageFile;
	}

}
