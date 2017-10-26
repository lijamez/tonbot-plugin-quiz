package net.tonbot.plugin.quiz;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class AnswerIncorrectEvent {

	private final long messageId;
}
