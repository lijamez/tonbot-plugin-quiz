package net.tonbot.plugin.trivia;

import java.util.Optional;

import com.google.common.base.Preconditions;

import lombok.Data;
import net.tonbot.plugin.trivia.model.Question;

@Data
public abstract class QuestionEndEvent<T extends Question> {

	private final T question;
	
	private final boolean timedOut;
	private final Win win;
	
	protected QuestionEndEvent(T question, boolean timedOut, Win win) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.timedOut = timedOut;
		this.win = win;
	}
	
	/**
	 * 
	 * @return The win details. Empty if no one answered correctly.
	 */
	public Optional<Win> getWin() {
		return Optional.ofNullable(win);
	}
}
