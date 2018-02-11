package net.tonbot.plugin.trivia;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.vdurmont.emoji.EmojiParser;

import net.tonbot.common.MessageNormalizer;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageTokenizer;

class DiscordMessageEventListener {

	private final IDiscordClient discordClient;
	private final TriviaSessionManager triviaSessionManager;

	@Inject
	public DiscordMessageEventListener(
			IDiscordClient discordClient,
			TriviaSessionManager triviaSessionManager) {
		this.discordClient = Preconditions.checkNotNull(discordClient,
				"discordClient must be non-null.");
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager,
				"triviaSessionManager must be non-null.");
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {

		// Ignore bots
		if (messageReceivedEvent.getAuthor().isBot()) {
			return;
		}

		TriviaSessionKey sessionKey = new TriviaSessionKey(
				messageReceivedEvent.getGuild().getLongID(),
				messageReceivedEvent.getChannel().getLongID());

		Optional<TriviaSession> optSession = triviaSessionManager.getSession(sessionKey);
		if (!optSession.isPresent()) {
			return;
		}

		TriviaSession session = optSession.get();
		IMessage message = messageReceivedEvent.getMessage();
		
		String normalizedMessageContent = normalize(message.getContent());
		
		if (StringUtils.isBlank(normalizedMessageContent)) {
			return;
		}
		
		UserMessage userMessage = UserMessage.builder()
				.message(normalizedMessageContent)
				.messageId(message.getLongID())
				.userId(messageReceivedEvent.getAuthor().getLongID())
				.build();
		session.takeInput(userMessage);
	}
	
	private String normalize(String message) {
		return MessageNormalizer.removeEmojis(message)
				.trim();
				
	}
}
