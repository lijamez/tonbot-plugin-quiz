package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.Optional;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;

class ShortAnswerQuestionHandler implements QuestionHandler {

	private final ShortAnswerQuestionTemplate question;
	private final TriviaListener listener;
	private final FuzzyMatcher fuzzyMatcher;

	public ShortAnswerQuestionHandler(ShortAnswerQuestionTemplate question, TriviaListener listener, LoadedTrivia loadedTrivia) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.fuzzyMatcher = new FuzzyMatcher(loadedTrivia.getTriviaTopic().getMetadata().getSynonyms());
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationSeconds, File imageFile) {
		ShortAnswerQuestionStartEvent event = ShortAnswerQuestionStartEvent.builder()
				.saQuestion(question)
				.maxDurationSeconds(maxDurationSeconds)
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.imageFile(imageFile).build();

		listener.onShortAnswerQuestionStart(event);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		Preconditions.checkNotNull(userMessage, "userMessage must be non-null.");

		return Optional.of(fuzzyMatcher.matches(userMessage.getMessage(), question.getAnswers()));
	}

	@Override
	public void notifyEnd(UserMessage userMessage, long pointsAwarded, long incorrectAttempts) {
		ShortAnswerQuestionEndEvent event = ShortAnswerQuestionEndEvent.builder()
				.acceptableAnswer(question.getAnswers().get(0))
				.win(
					userMessage != null
							? Win.builder().winningMessage(userMessage).pointsAwarded(pointsAwarded)
									.incorrectAttempts(incorrectAttempts).build()
							: null)
				.build();
		listener.onShortAnswerQuestionEnd(event);
	}
}
