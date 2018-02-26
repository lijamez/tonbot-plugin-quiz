package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
public class UserMessage {

	private final long userId;

	@NonNull
	private final String message;

	private final long messageId;

	@Builder
	private UserMessage(String message, Long messageId, Long userId) {
		this.message = Preconditions.checkNotNull(message, "message must be non-null.");
		this.messageId = Preconditions.checkNotNull(messageId, "messageId must be non-null.");
		this.userId = Preconditions.checkNotNull(userId, "userId must be non-null.");

	}
}
