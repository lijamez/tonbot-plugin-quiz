package net.tonbot.plugin.trivia;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class TriviaConfiguration {

	// The maximum number of questions to ask.
	private final int maxQuestions;

	// The amount of time in milliseconds to give to players per question.
	private final long defaultTimePerQuestion;

	// The maximum number of choices for multiple choice questions.
	private final int maxMultipleChoices;

	// The factor to reduce earned points by after answering incorrectly.
	private final double scoreDecayFactor;

	@NonNull
	private final String difficultyName;
}
