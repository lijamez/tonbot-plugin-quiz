package net.tonbot.plugin.trivia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestion;
import net.tonbot.plugin.trivia.model.Question;

class MultipleChoiceQuestionHandler implements QuestionHandler {

	private static final int MAX_CHOICES = 5;

	private final MultipleChoiceQuestion question;
	private final TriviaListener listener;
	private final List<Choice> choices;

	public MultipleChoiceQuestionHandler(MultipleChoiceQuestion question, TriviaListener listener) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.choices = getChoices(this.question);
	}

	@Override
	public Question getQuestion() {
		return question;
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationSeconds) {
		MultipleChoiceQuestionStartEvent startEvent = MultipleChoiceQuestionStartEvent.builder()
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.maxDurationSeconds(maxDurationSeconds)
				.mcQuestion(question)
				.choices(choices)
				.build();

		listener.onMultipleChoiceQuestionStart(startEvent);
	}

	/**
	 * Selects a list of choices. Only 1 of those choices are correct.
	 * 
	 * @param question
	 *            A {@link MultipleChoiceQuestion}.
	 * @return A list of choices, only 1 of which are correct.
	 */
	private List<Choice> getChoices(MultipleChoiceQuestion question) {
		List<Choice> incorrectChoices = pickRandomly(question.getIncorrectChoices(), MAX_CHOICES - 1);
		List<Choice> correctChoices = pickRandomly(question.getCorrectChoices(), 1);

		List<Choice> allChoices = new ArrayList<>();
		allChoices.addAll(incorrectChoices);
		allChoices.addAll(correctChoices);
		Collections.shuffle(allChoices);

		return allChoices;
	}

	private List<Choice> pickRandomly(List<Choice> list, int num) {
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
	public void notifyEnd(UserMessage userMessage) {
		Choice correctChoice = this.choices.stream()
				.filter(c -> c.isCorrect())
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No correct choice found."));

		MultipleChoiceQuestionEndEvent endEvent = MultipleChoiceQuestionEndEvent.builder()
				.correctChoice(correctChoice)
				.timedOut(userMessage == null)
				.answererId(userMessage != null ? userMessage.getUserId() : null)
				.build();

		listener.onMultipleChoiceQuestionEnd(endEvent);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		Preconditions.checkNotNull(userMessage, "userMessage must be non-null.");

		String msg = userMessage.getMessage();
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
