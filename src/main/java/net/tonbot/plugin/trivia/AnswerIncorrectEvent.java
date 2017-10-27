package net.tonbot.plugin.trivia;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class AnswerIncorrectEvent {

	private final long messageId;
}
