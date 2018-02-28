package net.tonbot.plugin.trivia;

import java.awt.Color;
import java.io.File;
import java.util.Random;
import java.util.Set;

import org.jaudiotagger.audio.AudioFileIO;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import sx.blah.discord.api.IDiscordClient;

class TriviaModule extends AbstractModule {

	private final IDiscordClient discordClient;
	private final BotUtils botUtils;
	private final Color color;
	private final File triviaTopicsDir;

	public TriviaModule(IDiscordClient discordClient, BotUtils botUtils, Color color, File triviaTopicsDir) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.color = Preconditions.checkNotNull(color, "color must be non-null.");
		this.triviaTopicsDir = Preconditions.checkNotNull(triviaTopicsDir, "triviaTopicsDir must be non-null.");
	}

	@Override
	protected void configure() {
		bind(IDiscordClient.class).toInstance(discordClient);
		bind(TriviaSessionManager.class).in(Scopes.SINGLETON);
		bind(BotUtils.class).toInstance(this.botUtils);
		bind(File.class).toInstance(triviaTopicsDir);
		bind(Color.class).toInstance(color);
	}

	@Provides
	@Singleton
	Set<Activity> activities(PlayActivity playActivity, TopicsActivity listActivity, StopActivity stopActivity) {
		return ImmutableSet.of(playActivity, listActivity, stopActivity);
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
}
