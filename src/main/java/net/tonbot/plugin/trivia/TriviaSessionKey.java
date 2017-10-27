package net.tonbot.plugin.trivia;

import lombok.Data;

@Data
class TriviaSessionKey {

	private final long guildId;
	private final long channelId;
}
