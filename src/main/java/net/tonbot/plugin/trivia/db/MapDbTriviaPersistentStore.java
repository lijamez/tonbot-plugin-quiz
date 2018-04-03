package net.tonbot.plugin.trivia.db;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import org.mapdb.DB;
import org.mapdb.Serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class MapDbTriviaPersistentStore implements TriviaPersistentStore {

	private static final String GUILD_USER_STATS_TABLE_NAME = "guild_user_stats";
	
	private final DB db;
	private final Map<String, byte[]> guildUserStatsTable;
	private final ObjectMapper objMapper;
	
	@Inject
	public MapDbTriviaPersistentStore(DB db, ObjectMapper objMapper) {
		this.db = Preconditions.checkNotNull(db, "db must be non-null.");
		this.objMapper = Preconditions.checkNotNull(objMapper, "objMapper must be non-null.");
		
		this.guildUserStatsTable = db.hashMap(GUILD_USER_STATS_TABLE_NAME, Serializer.STRING, Serializer.BYTE_ARRAY)
				.createOrOpen();
	}
	
	@Override
	public void addUserTriviaStats(long guildId, long userId, UserTriviaStats triviaStats) {
		Preconditions.checkNotNull(triviaStats, "triviaStats must be non-null.");
		
		String key = getGuildUserTriviaStatsKey(guildId, userId);
		
		byte[] serializedGuildUserStats = guildUserStatsTable.computeIfAbsent(key, k -> {
			
			GuildUserStats guserStats = new GuildUserStats(guildId, userId, new ArrayList<>());
			try {
				return objMapper.writeValueAsBytes(guserStats);
			} catch (JsonProcessingException e) {
				throw new UncheckedIOException(e);
			}
		});
		
		
		try {
			GuildUserStats guildUserStats = objMapper.readValue(serializedGuildUserStats, GuildUserStats.class);
			
			guildUserStats.getTriviaStats().add(triviaStats);
			
			byte[] newSerializedGuildUserStats = objMapper.writeValueAsBytes(guildUserStats);
			
			guildUserStatsTable.put(key, newSerializedGuildUserStats);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Override
	public Optional<GuildUserStats> getUserTriviaStats(long guildId, long userId) {
		
		String key = getGuildUserTriviaStatsKey(guildId, userId);
		byte[] serializedValue = guildUserStatsTable.get(key);
		if (serializedValue == null) {
			return Optional.empty();
		} else {
			try {
				GuildUserStats result = objMapper.readValue(serializedValue, GuildUserStats.class);
				return Optional.of(result);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	
	private String getGuildUserTriviaStatsKey(long guildId, long userId) {
		return Long.toString(guildId) + "_" + Long.toString(userId);
	}

	@Override
	public void close() {
		db.close();
	}
}
