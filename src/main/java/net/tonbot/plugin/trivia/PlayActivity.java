package net.tonbot.plugin.trivia;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.TriviaMetadata;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;

class PlayActivity implements Activity {

	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder()
			.route("trivia play")
			.parameters(ImmutableList.of("trivia name"))
			.description("Starts a round in the current channel.")
			.build();

	private final IDiscordClient discordClient;
	private final TriviaSessionManager triviaSessionManager;
	private final BotUtils botUtils;

	@Inject
	public PlayActivity(
			IDiscordClient discordClient,
			TriviaSessionManager triviaSessionManager,
			BotUtils botUtils) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager,
				"triviaSessionManager must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Override
	public void enact(MessageReceivedEvent event, String args) {
		String triviaPackName = args.trim();

		if (StringUtils.isBlank(triviaPackName)) {
			throw new TonbotBusinessException("You need to specify a trivia pack name.");
		}

		try {
			TriviaSessionKey sessionKey = new TriviaSessionKey(
					event.getGuild().getLongID(),
					event.getChannel().getLongID());
			triviaSessionManager.createSession(sessionKey, triviaPackName, new TriviaListener() {

				@Override
				public void onRoundStart(RoundStartEvent roundStartEvent) {
					TriviaMetadata metadata = roundStartEvent.getTriviaMetadata();
					String msg = "Starting " + metadata.getName() + "...";
					botUtils.sendMessageSync(event.getChannel(), msg);
				}

				@Override
				public void onRoundEnd(RoundEndEvent roundEndEvent) {
					Map<Long, Long> scores = roundEndEvent.getScores();
					StringBuilder sb = new StringBuilder();
					sb.append("Round finished!\n\nScores:\n");

					for (Entry<Long, Long> entry : scores.entrySet()) {
						IUser user = discordClient.fetchUser(entry.getKey());
						String displayName = user.getDisplayName(event.getGuild());
						long score = entry.getValue();

						sb.append(String.format("%s: %d points\n", displayName, score));
					}
					botUtils.sendMessageSync(event.getChannel(), sb.toString());
				}

				@Override
				public void onMultipleChoiceQuestionStart(
						MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent) {
					EmbedBuilder eb = getQuestionEmbedBuilder(multipleChoiceQuestionStartEvent);

					eb.withTitle(multipleChoiceQuestionStartEvent.getQuestion());

					StringBuilder sb = new StringBuilder();
					List<Choice> choices = multipleChoiceQuestionStartEvent.getChoices();
					for (int i = 0; i < choices.size(); i++) {
						Choice choice = choices.get(i);
						sb.append(String.format("``%d``: %s\n", i, choice.getValue()));
					}
					eb.withDescription(sb.toString());

					botUtils.sendEmbed(event.getChannel(), eb.build());
				}

				@Override
				public void onMultipleChoiceQuestionEnd(MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent) {
					Long answererId = multipleChoiceQuestionEndEvent.getAnswererId().orElse(null);
					Choice correctChoice = multipleChoiceQuestionEndEvent.getCorrectChoice();

					String msg;
					if (answererId == null) {
						msg = String.format("The correct answer was: **%s**", correctChoice.getValue());
					} else {
						IUser user = discordClient.fetchUser(answererId);
						msg = String.format("**%s** has answered correctly: **%s**",
								user.getDisplayName(event.getGuild()), correctChoice.getValue());
					}

					botUtils.sendMessageSync(event.getChannel(), msg);
				}

				@Override
				public void onShortAnswerQuestionStart(ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent) {
					EmbedBuilder eb = getQuestionEmbedBuilder(shortAnswerQuestionStartEvent);

					eb.withTitle(shortAnswerQuestionStartEvent.getQuestion());

					botUtils.sendEmbed(event.getChannel(), eb.build());
				}

				@Override
				public void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent) {

					UserMessage correctUserResponse = shortAnswerQuestionEndEvent.getCorrectUserResponse().orElse(null);
					String acceptableAnswer = shortAnswerQuestionEndEvent.getAcceptableAnswer();

					String msg;
					if (correctUserResponse == null) {
						msg = String.format("The correct answer was: **%s**", acceptableAnswer);
					} else {
						IUser user = discordClient.fetchUser(correctUserResponse.getUserId());
						msg = String.format("**%s** has answered correctly: **%s**",
								user.getDisplayName(event.getGuild()), correctUserResponse.getMessage());
					}

					botUtils.sendMessageSync(event.getChannel(), msg);
				}

				@Override
				public void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent) {
					// TODO
					botUtils.sendMessageSync(event.getChannel(), musicIdQuestionStartEvent.toString());
				}

				@Override
				public void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent) {
					// TODO
					botUtils.sendMessageSync(event.getChannel(), musicIdQuestionEndEvent.toString());
				}

				@Override
				public void onAnswerCorrect(AnswerCorrectEvent answerCorrectEvent) {
					IMessage message = discordClient.getMessageByID(answerCorrectEvent.getMessageId());
					if (message != null) {
						message.addReaction(ReactionEmoji.of("✅"));
					}
				}

				@Override
				public void onAnswerIncorrect(AnswerIncorrectEvent answerIncorrectEvent) {
					IMessage message = discordClient.getMessageByID(answerIncorrectEvent.getMessageId());
					if (message != null) {
						message.addReaction(ReactionEmoji.of("❌"));
					}
				}

				private EmbedBuilder getQuestionEmbedBuilder(QuestionStartEvent qse) {
					EmbedBuilder eb = new EmbedBuilder();
					eb.withFooterText(qse.getPoints() + " points");

					eb.withAuthorName(String.format("Question %d of %d",
							qse.getQuestionNumber(),
							qse.getTotalQuestions()));

					return eb;
				}

			});
		} catch (InvalidTriviaPackException e) {
			throw new TonbotBusinessException("Invalid trivia pack name.");
		}

	}
}
