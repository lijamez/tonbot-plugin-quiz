package net.tonbot.plugin.quiz;

import lombok.Data;

@Data
abstract class QuestionEndEvent {

	private final boolean timedOut;
}
