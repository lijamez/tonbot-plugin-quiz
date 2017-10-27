package net.tonbot.plugin.trivia;

import lombok.Data;

@Data
abstract class QuestionEndEvent {

	private final boolean timedOut;
}
