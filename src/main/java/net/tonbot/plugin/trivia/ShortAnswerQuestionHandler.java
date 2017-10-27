package net.tonbot.plugin.trivia;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.Question;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestion;

class ShortAnswerQuestionHandler implements QuestionHandler {

	private final ShortAnswerQuestion question;
	private final TriviaListener listener;

	public ShortAnswerQuestionHandler(ShortAnswerQuestion question, TriviaListener listener) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
	}

	@Override
	public Question getQuestion() {
		return question;
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationSeconds) {
		ShortAnswerQuestionStartEvent event = ShortAnswerQuestionStartEvent.builder()
				.maxDurationSeconds(maxDurationSeconds)
				.points(question.getPoints())
				.question(question.getQuestion())
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.build();

		listener.onShortAnswerQuestionStart(event);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		Preconditions.checkNotNull(userMessage, "userMessage must be non-null.");

		return Optional.of(isMatched(userMessage.getMessage()));
	}

	@Override
	public void notifyEnd(UserMessage userMessage) {
		ShortAnswerQuestionEndEvent event = ShortAnswerQuestionEndEvent.builder()
				.acceptableAnswer(question.getAnswers().get(0))
				.correctUserResponse(userMessage)
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
