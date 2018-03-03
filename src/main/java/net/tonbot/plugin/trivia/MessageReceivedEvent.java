package net.tonbot.plugin.trivia;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
class UserMessageReceivedEvent {

	@NonNull
	private final UserMessage userMessage;
	
	private final boolean isAnswer;
}
