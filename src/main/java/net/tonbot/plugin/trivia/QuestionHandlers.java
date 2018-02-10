package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate;
import net.tonbot.plugin.trivia.model.QuestionTemplate;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;

class QuestionHandlers {

	public static QuestionHandler get(QuestionTemplate question, TriviaConfiguration config, TriviaListener listener) {
		Preconditions.checkNotNull(question, "question must be non-null.");
		Preconditions.checkNotNull(config, "config must be non-null.");

		if (question instanceof MultipleChoiceQuestionTemplate) {
			return new MultipleChoiceQuestionHandler((MultipleChoiceQuestionTemplate) question, config, listener);
		} else if (question instanceof ShortAnswerQuestionTemplate) {
			return new ShortAnswerQuestionHandler((ShortAnswerQuestionTemplate) question, listener);
		} else {
			throw new IllegalArgumentException("Unsupported question type: " + question.getClass().getName());
		}
	}
}
