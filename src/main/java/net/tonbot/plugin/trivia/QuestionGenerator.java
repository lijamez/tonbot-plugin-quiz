package net.tonbot.plugin.trivia;

public interface QuestionGenerator<QT, Q> {

	/**
	 * Generates a question to ask, based off the given {@link QT}.
	 * 
	 * @param loadedTrivia
	 *            The {@link LoadedTrivia}. Non-null.
	 * @param config
	 *            The {@link TriviaConfiguration}. Non-null.
	 * @param questionTemplate
	 *            The {@link QT} to generate questions from. Non-null.
	 * @return A new {@link Q}.
	 */
	Q generate(LoadedTrivia loadedTrivia, TriviaConfiguration config, QT questionTemplate);
}
