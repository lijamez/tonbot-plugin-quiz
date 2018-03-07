package net.tonbot.plugin.trivia.shortanswer;

import java.io.File;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.LoadedTrivia;
import net.tonbot.plugin.trivia.QuestionGenerator;
import net.tonbot.plugin.trivia.TriviaConfiguration;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;

public class ShortAnswerQuestionGenerator implements QuestionGenerator<ShortAnswerQuestionTemplate, ShortAnswerQuestion> {

	private final Random random;
	
	@Inject
	public ShortAnswerQuestionGenerator(Random random) {
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
	}
	
	@Override
	public ShortAnswerQuestion generate(LoadedTrivia loadedTrivia, TriviaConfiguration config, ShortAnswerQuestionTemplate questionTemplate) {
		Preconditions.checkNotNull(loadedTrivia, "loadedTrivia must be non-null.");
		Preconditions.checkNotNull(questionTemplate, "questionTemplate must be non-null.");
		
		File imageFile = getRandomImageFile(questionTemplate, loadedTrivia.getTriviaTopicDir());
		
		return ShortAnswerQuestion.builder()
			.query(questionTemplate.getQuestion())
			.answers(questionTemplate.getAnswers())
			.image(imageFile)
			.points(questionTemplate.getPoints())
			.build();
	}
	
	private File getRandomImageFile(ShortAnswerQuestionTemplate q, File triviaRoot) {
		File imageFile = null;
		if (!q.getImagePaths().isEmpty()) {
			String imagePath = q.getImagePaths().get(random.nextInt(q.getImagePaths().size()));
			imageFile = new File(triviaRoot, imagePath);
		}

		return imageFile;
	}

}
