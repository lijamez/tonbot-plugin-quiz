package net.tonbot.plugin.trivia.db;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Data;
import net.tonbot.plugin.trivia.RoundRecord;

@Data
public class UserTriviaStats {

	@JsonProperty("topicName")
	private final String topicName;
	
	// The date and time of ending the game.
	@JsonProperty("endedAt")
	private final LocalDateTime endedAt;
	
	@JsonProperty("roundRecord")
	private final RoundRecord roundRecord;
	
	@JsonCreator
	public UserTriviaStats(
			@JsonProperty("topicName") String topicName,
			@JsonProperty("endedAt") LocalDateTime endedAt,
			@JsonProperty("roundRecord") RoundRecord roundRecord) {
		this.topicName = Preconditions.checkNotNull(topicName, "topicName must be non-null.");
		this.endedAt = Preconditions.checkNotNull(endedAt, "endedAt must be non-null.");
		this.roundRecord = Preconditions.checkNotNull(roundRecord, "roundRecords must be non-null.");
	}
}
