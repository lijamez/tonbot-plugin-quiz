package net.tonbot.plugin.trivia;

import java.awt.Color;
import java.io.File;
import java.util.Random;
import java.util.Set;

import org.jaudiotagger.audio.AudioFileIO;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import net.tonbot.common.Activity;
import net.tonbot.common.BotUtils;
import net.tonbot.plugin.trivia.db.MapDbTriviaPersistentStore;
import net.tonbot.plugin.trivia.db.TriviaPersistentStore;
import sx.blah.discord.api.IDiscordClient;

class TriviaModule extends AbstractModule {

	private final IDiscordClient discordClient;
	private final BotUtils botUtils;
	private final Color color;
	private final File triviaDataDir;

	public TriviaModule(IDiscordClient discordClient, BotUtils botUtils, Color color, File triviaDataDir) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.color = Preconditions.checkNotNull(color, "color must be non-null.");
		this.triviaDataDir = Preconditions.checkNotNull(triviaDataDir, "triviaDataDir must be non-null.");
	}

	@Override
	protected void configure() {
		bind(IDiscordClient.class).toInstance(discordClient);
		bind(TriviaSessionManager.class).in(Scopes.SINGLETON);
		bind(BotUtils.class).toInstance(this.botUtils);
		bind(File.class).toInstance(triviaDataDir);
		bind(Color.class).toInstance(color);
		bind(TriviaPersistentStore.class).to(MapDbTriviaPersistentStore.class);
	}

	@Provides
	@Singleton
	Set<Activity> activities(PlayActivity playActivity, TopicsActivity listActivity, StopActivity stopActivity, StatsActivity statsActivity) {
		return ImmutableSet.of(playActivity, listActivity, stopActivity, statsActivity);
	}

	@Provides
	@Singleton
	Set<Object> rawEventListeners(DiscordMessageEventListener discordMessageEventListener) {
		return ImmutableSet.of(discordMessageEventListener);
	}

	@Provides
	@Singleton
	ObjectMapper objectMapper() {
		ObjectMapper objMapper = new ObjectMapper();
		objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
		objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		objMapper.registerModule(new Jdk8Module());
		objMapper.registerModule(new JavaTimeModule());

		return objMapper;
	}

	@Provides
	@Singleton
	Random random() {
		return new Random(System.currentTimeMillis());
	}
	
	@Provides
	@Singleton
	AudioPlayerManager audioPlayerManager() {
		AudioPlayerManager apm = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerLocalSource(apm);
		return apm;
	}
	
	@Provides
	@Singleton
	AudioFileIO audioFileIO() {
		return new AudioFileIO();
	}
	
	@Provides
	@Singleton
	DB triviaDb() {
		File triviaPersistenceDir = new File(triviaDataDir, "persistence");
		triviaPersistenceDir.mkdirs();
		
		File triviaDbFile = new File(triviaPersistenceDir, "database.db");
		
		DB db = DBMaker.fileDB(triviaDbFile).make();
		return db;
	}
}
