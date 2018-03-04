package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.Enactable;
import net.tonbot.common.TonbotBusinessException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

class PlayActivity implements Activity {
	
	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("trivia play")
			.parameters(ImmutableList.of("<topic>", "[difficulty]"))
			.description("Starts a round in the current channel.")
			.build();

	private final TriviaSessionManager triviaSessionManager;
	private final TriviaListenerFactory triviaListenerFactory;

	@Inject
	public PlayActivity(IDiscordClient discordClient, TriviaSessionManager triviaSessionManager, TriviaListenerFactory triviaListenerFactory) {
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager, "triviaSessionManager must be non-null.");
		this.triviaListenerFactory = Preconditions.checkNotNull(triviaListenerFactory, "triviaListenerFactory must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Enactable(deleteCommand = true)
	public void enact(MessageReceivedEvent event, PlayRequest request) {
		
		try {
			TriviaSessionKey sessionKey = new TriviaSessionKey(event.getGuild().getLongID(),
					event.getChannel().getLongID());

			Difficulty effectiveDifficulty = request.getDifficulty() != null ? request.getDifficulty()
					: Difficulty.MEDIUM;
			
			TriviaListener triviaListener = triviaListenerFactory.createTriviaListener(event.getAuthor(), event.getChannel());
			triviaSessionManager.tryCreateSession(sessionKey, request.getTopic(), effectiveDifficulty, triviaListener);
		} catch (InvalidTopicException e) {
			throw new TonbotBusinessException(
					"Invalid topic name. Use the ``trivia topics`` command to see the available topics.");
		} catch (ExistingSessionException e) {
			throw new TonbotBusinessException("Please wait for the current round to end before starting another.");
		}
	}
}
