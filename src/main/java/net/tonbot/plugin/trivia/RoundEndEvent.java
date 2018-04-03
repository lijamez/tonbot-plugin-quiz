package net.tonbot.plugin.trivia;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
class RoundEndEvent {

	/**
	 * A map from user ID to their full scorekeeping record.
	 */
	@NonNull
	private final Map<Long, RoundRecord> scorekeepingRecords;
	
	@NonNull
	private final LoadedTrivia loadedTrivia;
	
	@NonNull
	private final TriviaConfiguration triviaConfig;
}
