package net.tonbot.plugin.trivia;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class AnswerCorrectEvent {

	private final long messageId;
}
