package net.tonbot.plugin.trivia;

import java.awt.Color;
import java.io.File;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;

import net.tonbot.common.Activity;
import net.tonbot.common.BotUtils;
import sx.blah.discord.api.IDiscordClient;

class TriviaModule extends AbstractModule {

	private final IDiscordClient discordClient;
	private final BotUtils botUtils;
	private final Color color;
	private final File triviaPacksDir;

	public TriviaModule(
			IDiscordClient discordClient,
			BotUtils botUtils,
			Color color,
			File triviaPacksDir) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.color = Preconditions.checkNotNull(color, "color must be non-null.");
		this.triviaPacksDir = Preconditions.checkNotNull(triviaPacksDir, "triviaPacksDir must be non-null.");
	}

	@Override
	protected void configure() {
		bind(IDiscordClient.class).toInstance(discordClient);
		bind(TriviaSessionManager.class).in(Scopes.SINGLETON);
		bind(BotUtils.class).toInstance(this.botUtils);
		bind(ThreadLocalRandom.class).toInstance(ThreadLocalRandom.current());
		bind(File.class).toInstance(triviaPacksDir);
		bind(Color.class).toInstance(color);
	}

	@Provides
	@Singleton
	Set<Activity> activities(PlayActivity playActivity, ListActivity listActivity) {
		return ImmutableSet.of(playActivity, listActivity);
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

		return objMapper;
	}
}
