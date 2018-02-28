package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.Enactable;
import net.tonbot.common.TonbotBusinessException;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

class StopActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("trivia stop")
			.parameters(ImmutableList.of())
			.description("Stops the current trivia round.")
			.build();
	
	private final TriviaSessionManager triviaSessionManager;
	
	@Inject
	public StopActivity(TriviaSessionManager triviaSessionManager) {
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager, "triviaSessionManager must be non-null.");
	}
	
	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}
	
	@Enactable
	public void enact(MessageReceivedEvent event) {

		TriviaSessionKey sessionKey = new TriviaSessionKey(event.getGuild().getLongID(), event.getChannel().getLongID());
		
		triviaSessionManager.stopSession(sessionKey)
			.orElseThrow(() -> new TonbotBusinessException("There is no trivia game running."));
	}
}
