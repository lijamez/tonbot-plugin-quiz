package net.tonbot.plugin.trivia;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Enactable;
import net.tonbot.plugin.trivia.db.GuildUserStats;
import net.tonbot.plugin.trivia.db.TriviaPersistentStore;
import net.tonbot.plugin.trivia.db.UserTriviaStats;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.EmbedBuilder;

class StatsActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("trivia stats")
			.description("Shows your stats.")
			.build();
	
	private final TriviaPersistentStore store;
	private final BotUtils botUtils;
	private final Color accent;
	
	@Inject
	public StatsActivity(TriviaPersistentStore store, BotUtils botUtils, Color accent) {
		this.store = Preconditions.checkNotNull(store, "store must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.accent = Preconditions.checkNotNull(accent, "accent must be non-null.");
	}
	
	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Enactable()
	public void enact(MessageReceivedEvent event) {
		
		long userId = event.getAuthor().getLongID();
		long guildId = event.getGuild().getLongID();
		
		GuildUserStats stats = store.getUserTriviaStats(guildId, userId)
				.orElse(null);
		
		if (stats == null) {
			String msg = String.format("%s, I don't have any stats for you!", event.getAuthor().getDisplayName(event.getGuild()));
			botUtils.sendMessage(event.getChannel(), msg);
			
			return;
		}
		
		EmbedBuilder eb = new EmbedBuilder();
		
		eb.withTitle(event.getAuthor().getDisplayName(event.getGuild()) + "'s All-time Trivia Stats");
		eb.withThumbnail(event.getAuthor().getAvatarURL());
		
		Map<String, List<UserTriviaStats>> groupedUserTriviaStats = stats.getTriviaStats().stream()
			.collect(Collectors.groupingBy(ts -> ts.getTopicName()));
		
		for (Entry<String, List<UserTriviaStats>> entry : groupedUserTriviaStats.entrySet()) {
			String topicName = entry.getKey();
			List<UserTriviaStats> userTriviaStatsList = entry.getValue();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("Times Played: **" + userTriviaStatsList.size() + "**");
			
			long totalEarnedPoints = userTriviaStatsList.stream()
				.mapToLong(s -> s.getRoundRecord().getTotalEarnedScore())
				.sum();
			long totalPossiblePoints = userTriviaStatsList.stream()
				.mapToLong(s -> s.getRoundRecord().getTotalPossibleScore())
				.sum();
			long totalPointsPercent = (long) ((totalEarnedPoints / (double) totalPossiblePoints) * 100);
			
			sb.append("\nScore: **" + totalEarnedPoints + "/" + totalPossiblePoints + " (" + totalPointsPercent + "%)**");
			
			
			long totalQuestionsAnswered = userTriviaStatsList.stream()
				.mapToLong(s -> s.getRoundRecord().getTotalCorrectlyAnsweredQuestions())
				.sum();
			long totalQuestions = userTriviaStatsList.stream()
				.mapToLong(s -> s.getRoundRecord().getTotalQuestions())
				.sum();
			long totalQuestionsAnsweredPercent = (long) ((totalQuestionsAnswered / (double) totalQuestions) * 100);
			
			sb.append("\nQuestions Answered: **" + totalQuestionsAnswered + "/" + totalQuestions + " (" + totalQuestionsAnsweredPercent + "%)**");
			
			OptionalDouble timeToAnswerAvgMs = userTriviaStatsList.stream()
				.flatMap(s -> s.getRoundRecord().getQuestionRecords().stream())
				.filter(qr -> qr.isAnsweredCorrectly() && (qr.getTimeToAnswerMs() != null))
				.mapToLong(qr -> qr.getTimeToAnswerMs())
				.average();
			String timeToAnswerSvgSecondsStr = timeToAnswerAvgMs.isPresent() ? String.format("%.2f", timeToAnswerAvgMs.getAsDouble() / 1000) : "N/A";
			
			sb.append("\nAverage Time to Answer: **" + timeToAnswerSvgSecondsStr + " seconds**");
			
			eb.appendField(topicName, sb.toString(), false);
		}
		
		eb.withColor(accent);
		
		botUtils.sendEmbed(event.getChannel(), eb.build());
	}
}
