package net.tonbot.plugin.trivia;

import java.util.Optional;

import lombok.Data;

@Data
abstract class QuestionEndEvent {

	private final boolean timedOut;
	private final Win win;

	/**
	 * 
	 * @return The win details. Empty if no one answered correctly.
	 */
	public Optional<Win> getWin() {
		return Optional.ofNullable(win);
	}
}
