package net.tonbot.plugin.trivia;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

class ListActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("trivia list")
			.parameters(ImmutableList.of())
			.description("Displays a list of trivia packs.")
			.build();

	private final TriviaLibrary triviaLibrary;
	private final BotUtils botUtils;

	@Inject
	public ListActivity(
			TriviaLibrary triviaLibrary,
			BotUtils botUtils) {
		this.triviaLibrary = Preconditions.checkNotNull(triviaLibrary, "triviaLibrary must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {

		Map<String, LoadedTrivia> triviaMap = triviaLibrary.getTrivia();

		StringBuilder sb = new StringBuilder();
		sb.append("**Trivia Packs:**\n\n");

		for (Entry<String, LoadedTrivia> entry : triviaMap.entrySet()) {
			sb.append(String.format("``%s``: %s\n",
					entry.getKey(),
					entry.getValue().getTriviaPack().getMetadata().getDescription()));
		}

		botUtils.sendMessage(event.getChannel(), sb.toString());
	}

}
