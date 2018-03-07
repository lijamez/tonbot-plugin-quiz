package net.tonbot.plugin.trivia.shortanswer;

import java.util.Optional;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.FuzzyMatcher;
import net.tonbot.plugin.trivia.LoadedTrivia;
import net.tonbot.plugin.trivia.QuestionHandler;
import net.tonbot.plugin.trivia.TriviaListener;
import net.tonbot.plugin.trivia.UserMessage;
import net.tonbot.plugin.trivia.Win;
import net.tonbot.plugin.trivia.model.Question;

public class ShortAnswerQuestionHandler implements QuestionHandler {

	private final ShortAnswerQuestion question;
	private final TriviaListener listener;
	private final FuzzyMatcher fuzzyMatcher;

	public ShortAnswerQuestionHandler(ShortAnswerQuestion question, TriviaListener listener, LoadedTrivia loadedTrivia) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		this.fuzzyMatcher = new FuzzyMatcher(loadedTrivia.getTriviaTopic().getMetadata().getSynonyms());
	}
	
	@Override
	public Question getQuestion() {
		return question;
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationSeconds) {
		ShortAnswerQuestionStartEvent event = ShortAnswerQuestionStartEvent.builder()
				.question(question)
				.maxDurationSeconds(maxDurationSeconds)
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.build();

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
				.question(question)
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
