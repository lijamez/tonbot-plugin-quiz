package net.tonbot.plugin.trivia.db;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Data;

@Data
public class GuildUserStats {

	@JsonProperty("guildId")
	private final long guildId;
	
	// User ID
	@JsonProperty("userId")
	private final long userId;
	
	@JsonProperty("triviaStats")
	private final List<UserTriviaStats> triviaStats;
	
	@JsonCreator
	GuildUserStats(
			@JsonProperty("guildId") long guildId,
			@JsonProperty("userId") long userId,
			@JsonProperty("triviaStats") List<UserTriviaStats> triviaStats) {
		this.guildId = guildId;
		this.userId = userId;
		this.triviaStats = Preconditions.checkNotNull(triviaStats, "triviaStats must be non-null.");
	}
}
