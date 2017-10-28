package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.MultipleChoiceQuestion;
import net.tonbot.plugin.trivia.model.Question;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestion;

class QuestionHandlers {

	public static QuestionHandler get(Question question, TriviaListener listener) {
		Preconditions.checkNotNull(question, "question must be non-null.");

		if (question instanceof MultipleChoiceQuestion) {
			return new MultipleChoiceQuestionHandler((MultipleChoiceQuestion) question, listener);
		} else if (question instanceof ShortAnswerQuestion) {
			return new ShortAnswerQuestionHandler((ShortAnswerQuestion) question, listener);
		} else {
			throw new IllegalArgumentException("Unsupport question type: " + question.getClass().getName());
		}
	}
}
