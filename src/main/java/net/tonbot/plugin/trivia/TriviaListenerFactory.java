package net.tonbot.plugin.trivia;

import java.awt.Color;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.tonbot.common.BotUtils;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

class TriviaListenerFactory {

	private final IDiscordClient discordClient;
	private final BotUtils botUtils;
	private final Color accentColor;
	private final AudioPlayerManager audioPlayerManager;
	
	@Inject
	public TriviaListenerFactory(IDiscordClient discordClient, BotUtils botUtils, Color accentColor, AudioPlayerManager audioPlayerManager) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");		
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.accentColor = Preconditions.checkNotNull(accentColor, "accentColor must be non-null.");
		this.audioPlayerManager = Preconditions.checkNotNull(audioPlayerManager, "audioPlayerManager must be non-null.");
	}
	
	/**
	 * Creates a new instance of a {@link TriviaListener} for the given context.
	 * @param initiator The {@link IUser} that started the trivia. Non-null.
	 * @param channel The {@link IChannel} that the trivia resides in. Non-null.
	 * @return A new {@link TriviaListener} instance.
	 */
	public TriviaListener createTriviaListener(IUser initiator, IChannel channel) {
		Preconditions.checkNotNull(initiator, "initiator must be non-null.");
		Preconditions.checkNotNull(channel, "channel must be non-null.");
		
		return new TriviaListenerImpl(discordClient, initiator, channel, botUtils, accentColor, audioPlayerManager);
	}
}
