package net.tonbot.plugin.quiz;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.quiz.model.MultipleChoiceQuestion;
import net.tonbot.plugin.quiz.model.Question;

class QuestionHandlers {

	public static QuestionHandler get(Question question, TriviaListener listener) {
		Preconditions.checkNotNull(question, "question must be non-null.");

		if (question instanceof MultipleChoiceQuestion) {
			return new MultipleChoiceQuestionHandler((MultipleChoiceQuestion) question, listener);
		} else {
			throw new IllegalArgumentException("Unsupport question type: " + question.getClass().getName());
		}
	}
}
