package net.tonbot.plugin.quiz;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
class UserMessage {

	private final long userId;

	@NonNull
	private final String message;

	private final long messageId;
}
