package net.tonbot.plugin.trivia.db;

import java.util.Optional;

public interface TriviaPersistentStore {

	/**
	 * Adds a {@link UserTriviaStats} to a user's {@link GuildUserStats}. A {@link GuildUserStats} is automatically created if none exists.
	 * @param guildId Guild ID.
	 * @param userId User ID.
	 * @param triviaStats {@link UserTriviaStats}. Non-null.
	 */
	void addUserTriviaStats(long guildId, long userId, UserTriviaStats triviaStats);
	
	/**
	 * Gets a user's trivia stats. 
	 * @param guild Guild ID.
	 * @param userId User ID. 
	 * @return {@link GuildUserStats}.
	 */
	Optional<GuildUserStats> getUserTriviaStats(long guild, long userId);
	
	/**
	 * Closes the store.
	 */
	void close();
}
