package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.QuestionTemplate;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;

class ShortAnswerQuestionHandler implements QuestionHandler {

	private final ShortAnswerQuestionTemplate question;
	private final TriviaListener listener;

	public ShortAnswerQuestionHandler(
			ShortAnswerQuestionTemplate question,
			TriviaListener listener) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
	}

	@Override
	public QuestionTemplate getQuestion() {
		return question;
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationSeconds, File imageFile) {
		ShortAnswerQuestionStartEvent event = ShortAnswerQuestionStartEvent.builder()
				.saQuestion(question)
				.maxDurationSeconds(maxDurationSeconds)
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.imageFile(imageFile)
				.build();

		listener.onShortAnswerQuestionStart(event);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		Preconditions.checkNotNull(userMessage, "userMessage must be non-null.");

		return Optional.of(isMatched(userMessage.getMessage()));
	}

	@Override
	public void notifyEnd(UserMessage userMessage, long pointsAwarded, long incorrectAttempts) {
		ShortAnswerQuestionEndEvent event = ShortAnswerQuestionEndEvent.builder()
				.acceptableAnswer(question.getAnswers().get(0))
				.win(userMessage != null ? Win.builder()
						.winningMessage(userMessage)
						.pointsAwarded(pointsAwarded)
						.incorrectAttempts(incorrectAttempts)
						.build()
						: null)
				.build();
		listener.onShortAnswerQuestionEnd(event);
	}

	private boolean isMatched(String messageStr) {
		String normalizedInput = normalize(messageStr);

		for (String candidate : question.getAnswers()) {
			String normalizedCandidate = normalize(candidate);

			if (StringUtils.equalsIgnoreCase(normalizedInput, normalizedCandidate)) {
				return true;
			}
		}

		return false;
	}

	private String normalize(String phrase) {
		String normalized = phrase.trim();

		// Multi-space characters should be replaced with a single space
		normalized = normalized.replaceAll("\\s+", " ");

		// Punctuation should be removed.
		normalized = normalized.replaceAll("\\p{Punct}", "");

		return normalized;
	}

}
