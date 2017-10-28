package net.tonbot.plugin.trivia;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
class RoundEndEvent {

	/**
	 * A map from user ID to score.
	 */
	@NonNull
	private final Map<Long, Long> scores;
}
