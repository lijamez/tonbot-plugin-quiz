package net.tonbot.plugin.quiz;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
class ShortAnswerUserResponse {

	/**
	 * A verbatim user response.
	 */
	@NonNull
	private final String userResponse;
	
	private final long userId;
}
