package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate;
import net.tonbot.plugin.trivia.model.QuestionTemplate;

class MultipleChoiceQuestionHandler implements QuestionHandler {

	private final MultipleChoiceQuestionTemplate question;
	private final TriviaListener listener;
	private final List<Choice> choices;

	public MultipleChoiceQuestionHandler(MultipleChoiceQuestionTemplate question, TriviaConfiguration config,
			TriviaListener listener) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.choices = getChoices(this.question, config.getMaxMultipleChoices());
	}

	@Override
	public QuestionTemplate getQuestion() {
		return question;
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationSeconds, File imageFile) {

		MultipleChoiceQuestionStartEvent startEvent = MultipleChoiceQuestionStartEvent.builder()
				.questionNumber(questionNumber).totalQuestions(totalQuestions).maxDurationSeconds(maxDurationSeconds)
				.imageFile(imageFile).mcQuestion(question).choices(choices).build();

		listener.onMultipleChoiceQuestionStart(startEvent);
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
	private List<Choice> getChoices(MultipleChoiceQuestionTemplate question, int maxChoices) {
		List<Choice> incorrectChoices = pickRandomSubset(question.getIncorrectChoices(), maxChoices - 1);
		List<Choice> correctChoices = pickRandomSubset(question.getCorrectChoices(), 1);

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

	@Override
	public void notifyEnd(UserMessage userMessage, long pointsAwarded, long incorrectAttempts) {
		Choice correctChoice = this.choices.stream().filter(c -> c.isCorrect()).findFirst()
				.orElseThrow(() -> new IllegalStateException("No correct choice found."));

		MultipleChoiceQuestionEndEvent endEvent = MultipleChoiceQuestionEndEvent.builder().correctChoice(correctChoice)
				.timedOut(userMessage == null).win(
						userMessage != null
								? Win.builder().pointsAwarded(pointsAwarded).incorrectAttempts(incorrectAttempts)
										.winningMessage(userMessage).build()
								: null)
				.build();

		listener.onMultipleChoiceQuestionEnd(endEvent);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		Preconditions.checkNotNull(userMessage, "userMessage must be non-null.");

		String msg = userMessage.getMessage().trim();
		try {
			int selectionNumber = Integer.parseInt(msg);

			if (selectionNumber < 0 || selectionNumber >= choices.size()) {
				return Optional.empty();
			}

			Choice selection = choices.get(selectionNumber);

			return Optional.of(selection.isCorrect());
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

}
