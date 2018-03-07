package net.tonbot.plugin.trivia.multiplechoice;

import java.util.Optional;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.QuestionHandler;
import net.tonbot.plugin.trivia.TriviaConfiguration;
import net.tonbot.plugin.trivia.TriviaListener;
import net.tonbot.plugin.trivia.UserMessage;
import net.tonbot.plugin.trivia.Win;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.Question;

public class MultipleChoiceQuestionHandler implements QuestionHandler {

	private final MultipleChoiceQuestion question;
	private final TriviaListener listener;

	public MultipleChoiceQuestionHandler(MultipleChoiceQuestion question, TriviaConfiguration config,
			TriviaListener listener) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
	}

	@Override
	public Question getQuestion() {
		return question;
	}
	
	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationMs) {

		MultipleChoiceQuestionStartEvent startEvent = MultipleChoiceQuestionStartEvent.builder()
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.maxDurationMs(maxDurationMs)
				.question(question)
				.build();

		listener.onMultipleChoiceQuestionStart(startEvent);
	}

	@Override
	public void notifyEnd(UserMessage userMessage, long pointsAwarded, long incorrectAttempts) {
		Choice correctChoice = this.question.getChoices().stream().filter(c -> c.isCorrect()).findFirst()
				.orElseThrow(() -> new IllegalStateException("No correct choice found."));

		MultipleChoiceQuestionEndEvent endEvent = MultipleChoiceQuestionEndEvent.builder()
				.question(question)
				.correctChoice(correctChoice)
				.timedOut(userMessage == null)
				.win(
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

			if (selectionNumber < 0 || selectionNumber >= question.getChoices().size()) {
				return Optional.empty();
			}

			Choice selection = question.getChoices().get(selectionNumber);

			return Optional.of(selection.isCorrect());
		} catch (NumberFormatException e) {
			return Optional.empty();
		}
	}

}
