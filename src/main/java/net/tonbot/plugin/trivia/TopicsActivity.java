package net.tonbot.plugin.trivia;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Enactable;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

class TopicsActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder().route("trivia topics")
			.parameters(ImmutableList.of()).description("Displays a list of trivia topics.").build();

	private final TriviaLibrary triviaLibrary;
	private final BotUtils botUtils;

	@Inject
	public TopicsActivity(TriviaLibrary triviaLibrary, BotUtils botUtils) {
		this.triviaLibrary = Preconditions.checkNotNull(triviaLibrary, "triviaLibrary must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Enactable
	public void enact(MessageReceivedEvent event) {

		Map<String, LoadedTrivia> triviaMap = triviaLibrary.getTrivia();

		EmbedBuilder eb = new EmbedBuilder();
		eb.withTitle("Trivia Topics");

		triviaMap.entrySet().stream()
			.sorted((a, b) -> a.getKey().compareTo(b.getKey()))
			.forEach(entry -> {
				eb.appendField(entry.getKey(), entry.getValue().getTriviaTopic().getMetadata().getDescription(), false);
			});

		eb.withFooterText("Use the ``trivia play`` command to play them.");

		botUtils.sendEmbed(event.getChannel(), eb.build());
	}

}
