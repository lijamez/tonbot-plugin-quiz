package net.tonbot.plugin.trivia;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.tonbot.common.Activity;
import net.tonbot.common.ActivityDescriptor;
import net.tonbot.common.BotUtils;
import net.tonbot.common.Enactable;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;
import net.tonbot.plugin.trivia.model.TriviaMetadata;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionEndEvent;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionStartEvent;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;

class PlayActivity implements Activity {
	
	private static final Logger LOG = LoggerFactory.getLogger(PlayActivity.class);
	private static final ActivityDescriptor ACTIVITY_DESCRIPTOR = ActivityDescriptor.builder().route("trivia play")
			.parameters(ImmutableList.of("<topic>", "[difficulty]"))
			.description("Starts a round in the current channel.").build();

	private final IDiscordClient discordClient;
	private final TriviaSessionManager triviaSessionManager;
	private final BotUtils botUtils;
	private final Color color;
	private final AudioPlayerManager apm;

	@Inject
	public PlayActivity(IDiscordClient discordClient, TriviaSessionManager triviaSessionManager, BotUtils botUtils,
			Color color, AudioPlayerManager apm) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.triviaSessionManager = Preconditions.checkNotNull(triviaSessionManager,
				"triviaSessionManager must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.color = Preconditions.checkNotNull(color, "color must be non-null.");
		this.apm = Preconditions.checkNotNull(apm, "apm must be non-null.");
	}

	@Override
	public ActivityDescriptor getDescriptor() {
		return ACTIVITY_DESCRIPTOR;
	}

	@Enactable
	public void enact(MessageReceivedEvent event, PlayRequest request) {
		
		try {
			TriviaSessionKey sessionKey = new TriviaSessionKey(event.getGuild().getLongID(),
					event.getChannel().getLongID());

			Difficulty effectiveDifficulty = request.getDifficulty() != null ? request.getDifficulty()
					: Difficulty.MEDIUM;

			triviaSessionManager.createSession(sessionKey, request.getTopic(), effectiveDifficulty,
					new TriviaListener() {
				
						private final AudioManager audioManager = new AudioManager(event.getGuild(), apm);
				
						@Override
						public void onRoundStart(RoundStartEvent roundStartEvent) {
							
							if (roundStartEvent.isHasAudio()) {
								IVoiceChannel voiceChannel = event.getAuthor().getVoiceStateForGuild(event.getGuild()).getChannel();
								if (voiceChannel == null) {
									throw new TonbotBusinessException("That topic needs audio. Please join a voice channel first.");
								}
								
								try {
									audioManager.joinVC(voiceChannel);
								} catch (MissingPermissionsException e) {
									throw new TonbotBusinessException("I'm not allowed to connect to that voice channel.");
								} catch (AlreadyInAnotherVoiceChannelException e) {
									throw new TonbotBusinessException("I can't join your voice channel because I'm connected to ``" + e.getVoiceChannel().getName() + "``");
								}
							}
							
							TriviaMetadata metadata = roundStartEvent.getTriviaMetadata();
							String msg = String.format(
									":checkered_flag: Starting ``%s`` on %s difficulty...",
									metadata.getName(), roundStartEvent.getDifficultyName());
							botUtils.sendMessageSync(event.getChannel(), msg);
						}

						@Override
						public void onRoundEnd(RoundEndEvent roundEndEvent) {
							audioManager.leaveVC();
							
							Map<Long, Long> scores = roundEndEvent.getScores();
							EmbedBuilder eb = new EmbedBuilder();
							eb.withColor(color);
							eb.withTitle(":triangular_flag_on_post: Round finished!");

							StringBuilder scoresSb = new StringBuilder();
							if (scores.isEmpty()) {
								scoresSb.append("No participants :(");
							} else {
								List<Entry<Long, Long>> ranking = scores.entrySet().stream().sorted((x, y) -> {
									return (int) (y.getValue() - x.getValue());
								}).collect(Collectors.toList());

								long highestScore = ranking.size() > 0 ? ranking.get(0).getValue() : 0;

								for (Entry<Long, Long> entry : ranking) {
									IUser user = discordClient.fetchUser(entry.getKey());
									String displayName = user.getDisplayName(event.getGuild());
									long score = entry.getValue();

									if (score == highestScore) {
										scoresSb.append(":trophy: ");
									}
									scoresSb.append(String.format("%s: %d points\n", displayName, score));
								}
							}

							eb.appendField("Scoreboard", scoresSb.toString(), false);

							botUtils.sendEmbed(event.getChannel(), eb.build());
						}
						
						@Override
						public void onCrash() {
							botUtils.sendMessage(event.getChannel(), "The trivia has crashed. :(");
						}

						@Override
						public void onMultipleChoiceQuestionStart(
								MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent) {
							EmbedBuilder eb = getQuestionEmbedBuilder(multipleChoiceQuestionStartEvent);
							MultipleChoiceQuestionTemplate mcQuestion = multipleChoiceQuestionStartEvent
									.getMultipleChoiceQuestion();
							eb.withColor(color);
							eb.withTitle(mcQuestion.getQuestion());

							StringBuilder sb = new StringBuilder();
							List<Choice> choices = multipleChoiceQuestionStartEvent.getChoices();
							for (int i = 0; i < choices.size(); i++) {
								Choice choice = choices.get(i);
								sb.append(String.format("``%d``: %s\n", i, choice.getValue()));
							}
							eb.withDescription(sb.toString());

							sendQuestionEmbed(eb, multipleChoiceQuestionStartEvent);
						}

						@Override
						public void onMultipleChoiceQuestionEnd(
								MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent) {
							Win win = multipleChoiceQuestionEndEvent.getWin().orElse(null);
							Choice correctChoice = multipleChoiceQuestionEndEvent.getCorrectChoice();

							String msg = getStandardRoundEndMessage(win, correctChoice.getValue());

							botUtils.sendMessageSync(event.getChannel(), msg);
						}

						@Override
						public void onShortAnswerQuestionStart(
								ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent) {
							EmbedBuilder eb = getQuestionEmbedBuilder(shortAnswerQuestionStartEvent);
							ShortAnswerQuestionTemplate saQuestion = shortAnswerQuestionStartEvent
									.getShortAnswerQuestion();

							// If the question has newlines, put everything up to the first newline
							// character in the embed's title.
							// The rest does in the embed's description.
							String question = saQuestion.getQuestion();
							int newlineIndex = question.indexOf('\n');
							if (newlineIndex >= 0) {
								String mainQuestion = question.substring(0, newlineIndex);
								String extra = question.substring(newlineIndex, question.length());
								eb.withTitle(mainQuestion);
								eb.withDescription(extra);
							} else {
								eb.withTitle(saQuestion.getQuestion());
							}

							sendQuestionEmbed(eb, shortAnswerQuestionStartEvent);
						}

						@Override
						public void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent) {
							Win win = shortAnswerQuestionEndEvent.getWin().orElse(null);
							String acceptableAnswer = shortAnswerQuestionEndEvent.getAcceptableAnswer();

							String msg = getStandardRoundEndMessage(win,
									win != null ? win.getWinningMessage().getMessage() : acceptableAnswer);

							botUtils.sendMessageSync(event.getChannel(), msg);
						}

						@Override
						public void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent) {
							AudioTrack audioTrack = audioManager.findTrack(musicIdQuestionStartEvent.getAudioFile());
							long maxPosition = Math.max(audioTrack.getDuration() - musicIdQuestionStartEvent.getMaxDurationMs(), 0);
							long randomPosition = (long) (Math.random() * maxPosition);
							audioManager.playInVC(audioTrack, randomPosition);
							
							EmbedBuilder eb = getQuestionEmbedBuilder(musicIdQuestionStartEvent);
							
							String question = null;
							switch (musicIdQuestionStartEvent.getTagToAsk()) {
							case TITLE:
								question = "What's the title of this track?";
								break;
							case ALBUM:
								question = "What album did this track originally come from?";
								break;
							case COMPOSER:
								question = "Who is the composer of this track?";
								break;
							default:
								question = "What is the " + musicIdQuestionStartEvent.getTagToAsk() + " of this track?";
								break;
							}
							eb.withTitle("🎵 " + question);
							
							LOG.info(musicIdQuestionStartEvent.toString());
							botUtils.sendEmbed(event.getChannel(), eb.build());
						}

						@Override
						public void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent) {
							audioManager.stopPlaying();
							
							Win win = musicIdQuestionEndEvent.getWin().orElse(null);
							String canonicalAnswer = musicIdQuestionEndEvent.getCanonicalAnswer();

							String msg = getStandardRoundEndMessage(win,
									win != null ? win.getWinningMessage().getMessage() : canonicalAnswer);

							botUtils.sendMessageSync(event.getChannel(), msg);
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

						/**
						 * Sends a question embed. This method will add an image, if the
						 * {@link QuestionStartEvent} has one.
						 * 
						 * @param eb
						 *            The {@link EmbedBuilder}. Non-null.
						 * @param qse
						 *            The {@link QuestionStartEvent}. Non-null.
						 */
						private void sendQuestionEmbed(EmbedBuilder eb, QuestionStartEvent qse) {
							File imageFile = qse.getImage().orElse(null);

							if (imageFile != null) {
								String extension = FilenameUtils.getExtension(imageFile.getName());
								String baseName = UUID.randomUUID().toString();
								String discordFriendlyFileName = baseName.replaceAll("[^A-Za-z0-9]", "") + "."
										+ extension;

								eb.withImage("attachment://" + discordFriendlyFileName);

								try {
									FileInputStream fis = new FileInputStream(imageFile);
									botUtils.sendEmbed(event.getChannel(), eb.build(), fis, discordFriendlyFileName);
								} catch (FileNotFoundException e) {
									// Should never happen because TriviaLibrary performs a file existence check.
									throw new UncheckedIOException(e);
								}

							} else {
								botUtils.sendEmbed(event.getChannel(), eb.build());
							}
						}

						/**
						 * Gets a {@link EmbedBuilder} with some settings already set, such as color,
						 * footer text, and question number.
						 * 
						 * @param qse
						 *            {@link QuestionStartEvent}. Non-null.
						 * @return A {@link EmbedBuilder} with some settings already set.
						 */
						private EmbedBuilder getQuestionEmbedBuilder(QuestionStartEvent qse) {
							EmbedBuilder eb = new EmbedBuilder();

							eb.withColor(color);
							eb.withFooterText(
									String.format("First to correctly answer within %d seconds wins %d points",
											qse.getMaxDurationMs() / 1000, qse.getQuestion().getPoints()));

							eb.withAuthorName(String.format("Question %d of %d", qse.getQuestionNumber(),
									qse.getTotalQuestions()));

							return eb;
						}

						private String getStandardRoundEndMessage(Win win, String correctAnswer) {
							String msg;
							if (win == null) {
								msg = String.format(":alarm_clock: Time's up! The correct answer was: **%s**",
										correctAnswer);
							} else {
								IUser user = discordClient.fetchUser(win.getWinnerUserId());
								StringBuilder sb = new StringBuilder();
								sb.append(String.format("**%s** wins %d point(s) for the answer ``%s``",
										user.getDisplayName(event.getGuild()), win.getPointsAwarded(), correctAnswer));

								if (win.getIncorrectAttempts() > 0) {
									sb.append(" after ").append(win.getIncorrectAttempts());
									if (win.getIncorrectAttempts() > 1) {
										sb.append(" incorrect attempts");
									} else {
										sb.append(" incorrect attempt");
									}
								}
								sb.append(".");
								msg = sb.toString();
							}

							return msg;
						}
						
						
					});
		} catch (InvalidTopicException e) {
			throw new TonbotBusinessException(
					"Invalid topic name. Use the ``trivia topics`` command to see the available topics.");
		}
	}
}
