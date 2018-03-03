package net.tonbot.plugin.trivia;

import java.util.Optional;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

class DiscordMessageEventListener {
	
	private final TriviaSessionManager triviaSessionManager;

	@Inject
	public DiscordMessageEventListener(TriviaSessionManager triviaSessionManager) {
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager,
				"triviaSessionManager must be non-null.");
	}

	@EventSubscriber
	public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {

		// Ignore bots
		if (messageReceivedEvent.getAuthor().isBot()) {
			return;
		}
		
		IMessage message = messageReceivedEvent.getMessage();
		String messageContent = messageReceivedEvent.getMessage().getContent();

		TriviaSessionKey sessionKey = new TriviaSessionKey(messageReceivedEvent.getGuild().getLongID(),
				messageReceivedEvent.getChannel().getLongID());

		Optional<TriviaSession> optSession = triviaSessionManager.getSession(sessionKey);
		if (!optSession.isPresent()) {
			return;
		}

		TriviaSession session = optSession.get();

		UserMessage userMessage = UserMessage.builder()
				.message(messageContent)
				.messageId(message.getLongID())
				.userId(messageReceivedEvent.getAuthor().getLongID())
				.build();
		
		session.takeInput(userMessage);
	}
}
