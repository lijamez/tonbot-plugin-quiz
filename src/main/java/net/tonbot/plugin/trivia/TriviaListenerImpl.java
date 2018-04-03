package net.tonbot.plugin.trivia;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.tonbot.common.BotUtils;
import net.tonbot.common.TonbotBusinessException;
import net.tonbot.plugin.trivia.db.TriviaPersistentStore;
import net.tonbot.plugin.trivia.db.UserTriviaStats;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.TriviaMetadata;
import net.tonbot.plugin.trivia.multiplechoice.MultipleChoiceQuestion;
import net.tonbot.plugin.trivia.multiplechoice.MultipleChoiceQuestionEndEvent;
import net.tonbot.plugin.trivia.multiplechoice.MultipleChoiceQuestionStartEvent;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestion;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionEndEvent;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionStartEvent;
import net.tonbot.plugin.trivia.musicid.SongMetadata;
import net.tonbot.plugin.trivia.musicid.SongProperty;
import net.tonbot.plugin.trivia.shortanswer.ShortAnswerQuestion;
import net.tonbot.plugin.trivia.shortanswer.ShortAnswerQuestionEndEvent;
import net.tonbot.plugin.trivia.shortanswer.ShortAnswerQuestionStartEvent;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RequestBuilder;

class TriviaListenerImpl implements TriviaListener {

	private static final Logger LOG = LoggerFactory.getLogger(TriviaListenerImpl.class);
	
	private static final List<SongProperty> MUSIC_ID_QUESTION_END_SONG_FIELDS = ImmutableList.of(
			SongProperty.TITLE,
			SongProperty.ALBUM,
			SongProperty.ARTIST,
			SongProperty.COMPOSER);
	
	private static final long FINAL_RESULTS_TTL = 60;
	private static final TimeUnit FINAL_RESULTS_TTL_UNIT = TimeUnit.SECONDS;
	
	private final IDiscordClient discordClient;
	private final IUser initiator;
	private final IChannel channel;
	private final BotUtils botUtils;
	private final Color accentColor;
	private final AudioManager audioManager;
	private final ConcurrentLinkedQueue<IMessage> deletableMessages = new ConcurrentLinkedQueue<>();
	private final TriviaPersistentStore store;
	
	private LoadedAudioCues audioCues;

	public TriviaListenerImpl(IDiscordClient discordClient, IUser initiator, IChannel channel, BotUtils botUtils, Color accentColor, AudioPlayerManager apm, TriviaPersistentStore store) {
		this.discordClient = Preconditions.checkNotNull(discordClient, "discordClient must be non-null.");
		this.initiator = Preconditions.checkNotNull(initiator, "initiator must be non-null.");
		this.channel = Preconditions.checkNotNull(channel, "channel must be non-null.");
		this.botUtils = Preconditions.checkNotNull(botUtils, "botUtils must be non-null.");
		this.accentColor = Preconditions.checkNotNull(accentColor, "accentColor must be non-null.");
		this.audioManager = new AudioManager(channel.getGuild(), apm);
		this.store = Preconditions.checkNotNull(store, "store must be non-null.");
	}
	
	@Override
	public void onRoundStart(RoundStartEvent roundStartEvent) {
		this.audioCues = roundStartEvent.getAudioCues();
		
		if (roundStartEvent.isHasAudio()) {
			IVoiceChannel voiceChannel = initiator.getVoiceStateForGuild(channel.getGuild()).getChannel();
			if (voiceChannel == null) {									
				throwNeedVoiceChannelException(
						"The topic ``" + roundStartEvent.getTriviaMetadata().getName() 
						+ "`` has audio. Please join a voice channel first.", 
						channel.getGuild());
			}
			
			try {
				audioManager.joinVC(voiceChannel);
				audioCues.getRoundStart().ifPresent(audioManager::playInVC);
			} catch (MissingPermissionsException e) {
				throwNeedVoiceChannelException("I'm not allowed to connect to your voice channel.", channel.getGuild());
			} catch (AlreadyInAnotherVoiceChannelException e) {
				throw new TonbotBusinessException("I can't join your voice channel because I'm connected to :sound:``" + e.getVoiceChannel().getName() + "``");
			}
		}
		
		TriviaMetadata metadata = roundStartEvent.getTriviaMetadata();
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.withTitle(":checkered_flag: " + metadata.getName());
		eb.appendDesc(metadata.getDescription() + "\n\n" 
				+ String.format("**Submit your answer by ending it with ``%s`` (an exclamation mark)!**", Constants.ANSWER_SUFFIX));
		eb.appendField("Difficulty", roundStartEvent.getDifficultyName(), true);
		eb.appendField("Starting in", String.format("%d seconds", roundStartEvent.getStartingInMs() / 1000), true);
		eb.withFooterText("Started by " + initiator.getDisplayName(channel.getGuild()));
		eb.withFooterIcon(initiator.getAvatarURL());
		eb.withColor(accentColor);
		
		IMessage message;
		if (roundStartEvent.getIcon().isPresent()) {
			File icon = roundStartEvent.getIcon().get();
			
			String fileName = icon.getName();
			eb.withThumbnail("attachment://" + fileName);
			
			try {
				FileInputStream fis = new FileInputStream(icon);
				message = botUtils.sendEmbedSync(channel, eb.build(), fis, fileName);
			} catch (FileNotFoundException e) {
				LOG.warn("Unable to read trivia icon.", e);
				message = botUtils.sendEmbedSync(channel, eb.build());
			}
			
		} else {
			message = botUtils.sendEmbedSync(channel, eb.build());
		}
		
		deletableMessages.add(message);
	}
	
	private void throwNeedVoiceChannelException(String message, IGuild guild) {
		IUser self = discordClient.getOurUser();
		List<String> connectableVcNames = guild.getVoiceChannels().stream()
				.filter(vc -> vc.getModifiedPermissions(self).contains(Permissions.VOICE_CONNECT))
				.map(vc -> ":sound: " + vc.getName())
				.collect(Collectors.toList());
		
		StringBuilder sb = new StringBuilder();
		sb.append(":x: ");
		sb.append(message);
		
		if (!connectableVcNames.isEmpty()) {
			sb.append(" Try one of these instead:\n" + StringUtils.join(connectableVcNames, "\n"));
		} else {
			sb.append("\nUnfortunately, it seems like I can't connect to any of the voice channels here.");
		}
		
		throw new TonbotBusinessException(sb.toString());
	}

	@Override
	public void onRoundEnd(RoundEndEvent roundEndEvent) {
		LOG.info("Round has ended.");
		purgeDeletableMessagesAsync();
		
		Map<Long, RoundRecord> scorekeepingRecords = roundEndEvent.getScorekeepingRecords();
		EmbedBuilder eb = new EmbedBuilder();
		eb.withColor(accentColor);
		eb.withTitle(":triangular_flag_on_post: Round finished!");

		eb.appendField("Topic", roundEndEvent.getLoadedTrivia().getTriviaTopic().getMetadata().getName(), true);
		eb.appendField("Difficulty", roundEndEvent.getTriviaConfig().getDifficultyName(), true);
		
		StringBuilder scoresSb = new StringBuilder();

		if (scorekeepingRecords.isEmpty()) {
			scoresSb.append("No participants :frowning:");
		} else {
			List<Entry<Long, RoundRecord>> rankings = scorekeepingRecords.entrySet().stream()
					.sorted((x, y) -> {
						RoundRecord xRecord = x.getValue();
						RoundRecord yRecord = y.getValue();
						
						return (int) (yRecord.getTotalEarnedScore() - xRecord.getTotalEarnedScore());
					})
					.collect(Collectors.toList());
			
			for (int rank = 0; rank < rankings.size(); rank++) {
				Entry<Long, RoundRecord> entry = rankings.get(rank);
				RoundRecord record = entry.getValue();
				
				IUser user = discordClient.fetchUser(entry.getKey());
				String displayName = user.getDisplayName(channel.getGuild());
				
				if (rank == 0) {
					scoresSb.append("**");
				}
				
				if (rank == 0) {
					scoresSb.append(":first_place: ");
				} else if (rank == 1) {
					scoresSb.append(":second_place: ");
				} else if (rank == 2) {
					scoresSb.append(":third_place: ");
				} else {
					scoresSb.append(":white_small_square: ");
				}
				
				scoresSb.append(String.format("%s: %d/%d points (%d/%d Correct)", 
						displayName, 
						record.getTotalEarnedScore(), 
						record.getTotalPossibleScore(), 
						record.getTotalCorrectlyAnsweredQuestions(), 
						record.getTotalQuestions()));
				
				if (rank == 0) {
					scoresSb.append("** :trophy:");
				}
				
				scoresSb.append("\n");
			}
		}

		eb.appendField("Scoreboard", scoresSb.toString(), false);

		botUtils.sendEmbed(channel, eb.build(), FINAL_RESULTS_TTL, FINAL_RESULTS_TTL_UNIT);
		
		saveToDB(
				channel.getGuild().getLongID(), 
				scorekeepingRecords, 
				roundEndEvent.getLoadedTrivia().getTriviaTopic().getMetadata().getName());
		
		File roundCompleteAudioCue = audioCues.getRoundComplete().orElse(null);
		if (roundCompleteAudioCue != null) {
			AudioTrack at = audioManager.findTrack(roundCompleteAudioCue);
			long durationMs = at.getDuration();
			audioManager.playInVC(at, 0);
			try {
				Thread.sleep(durationMs + 500);
			} catch (InterruptedException e) {
				LOG.warn("TriviaListener thread was interrupted from sleep.", e);
			}
		}
		
		audioManager.leaveVC();
	}
	
	private void saveToDB(long guildId, Map<Long, RoundRecord> scorekeepingRecords, String triviaName) {
		LocalDateTime now = LocalDateTime.now();
		
		for (Entry<Long, RoundRecord> entry : scorekeepingRecords.entrySet()) {
			long userId = entry.getKey();
			RoundRecord record = entry.getValue();
			
			UserTriviaStats uts = new UserTriviaStats(
					triviaName, 
					now, 
					record);
			store.addUserTriviaStats(guildId, userId, uts);
		}
	}
	
	@Override
	public void onUserMessageReceived(UserMessageReceivedEvent umre) {
		if (umre.isAnswer()) {
			IMessage message = discordClient.getMessageByID(umre.getUserMessage().getMessageId());
			deletableMessages.add(message);
		}
	}
	
	@Override
	public void onCrash() {
		audioManager.leaveVC();
		
		botUtils.sendMessage(channel, "The trivia has crashed. :(");
	}

	@Override
	public void onMultipleChoiceQuestionStart(
			MultipleChoiceQuestionStartEvent multipleChoiceQuestionStartEvent) {
		purgeDeletableMessagesAsync();
		
		EmbedBuilder eb = getQuestionEmbedBuilder(multipleChoiceQuestionStartEvent);
		MultipleChoiceQuestion mcQuestion = multipleChoiceQuestionStartEvent.getQuestion();
		eb.withColor(accentColor);
		
		String query = mcQuestion.getQuery();
		
		int newlineIndex = query.indexOf('\n');
		if (newlineIndex > 0) {
			String mainQuestion = query.substring(0, newlineIndex);
			String extra = query.substring(newlineIndex, query.length());
			eb.withTitle(mainQuestion);
			eb.withDescription(extra);
		} else {
			eb.withTitle(query);
		}
		

		StringBuilder choicesSb = new StringBuilder();
		List<Choice> choices = mcQuestion.getChoices();
		for (int i = 0; i < choices.size(); i++) {
			Choice choice = choices.get(i);
			choicesSb.append(String.format("``%d``: %s\n", i, choice.getValue()));
		}
		eb.appendField("--------------------", choicesSb.toString(), false);

		IMessage message = sendQuestionEmbed(eb, multipleChoiceQuestionStartEvent);
		deletableMessages.add(message);
	}

	@Override
	public void onMultipleChoiceQuestionEnd(
			MultipleChoiceQuestionEndEvent multipleChoiceQuestionEndEvent) {
		
		Win win = multipleChoiceQuestionEndEvent.getWin().orElse(null);
		Choice correctChoice = multipleChoiceQuestionEndEvent.getCorrectChoice();

		String msg = getStandardRoundEndMessage(win, ImmutableList.of(correctChoice.getValue()), correctChoice.getValue());

		IMessage message = botUtils.sendMessageSync(channel, msg);
		deletableMessages.add(message);
	}

	@Override
	public void onShortAnswerQuestionStart(
			ShortAnswerQuestionStartEvent shortAnswerQuestionStartEvent) {
		purgeDeletableMessagesAsync();
		
		EmbedBuilder eb = getQuestionEmbedBuilder(shortAnswerQuestionStartEvent);
		ShortAnswerQuestion question = shortAnswerQuestionStartEvent
				.getQuestion();

		// If the question has newlines, put everything up to the first newline
		// character in the embed's title.
		// The rest does in the embed's description.
		String query = question.getQuery();
		int newlineIndex = query.indexOf('\n');
		if (newlineIndex >= 0) {
			String mainQuestion = query.substring(0, newlineIndex);
			String extra = query.substring(newlineIndex, query.length());
			eb.withTitle(mainQuestion);
			eb.withDescription(extra);
		} else {
			eb.withTitle(query);
		}

		IMessage message = sendQuestionEmbed(eb, shortAnswerQuestionStartEvent);
		deletableMessages.add(message);
	}

	@Override
	public void onShortAnswerQuestionEnd(ShortAnswerQuestionEndEvent shortAnswerQuestionEndEvent) {
		
		Win win = shortAnswerQuestionEndEvent.getWin().orElse(null);
		String acceptableAnswer = shortAnswerQuestionEndEvent.getAcceptableAnswer();

		String msg = getStandardRoundEndMessage(win, ImmutableList.of(acceptableAnswer), null);

		IMessage message = botUtils.sendMessageSync(channel, msg);
		deletableMessages.add(message);
	}

	@Override
	public void onMusicIdQuestionStart(MusicIdQuestionStartEvent musicIdQuestionStartEvent) {
		purgeDeletableMessagesAsync();
		
		MusicIdQuestion question = musicIdQuestionStartEvent.getQuestion();
		
		AudioTrack audioTrack = audioManager.findTrack(question.getAudioFile());
		long maxPosition = Math.max(audioTrack.getDuration() - musicIdQuestionStartEvent.getMaxDurationMs(), 0);
		long randomPosition = (long) (Math.random() * maxPosition);
		audioManager.playInVC(audioTrack, randomPosition);
		
		EmbedBuilder eb = getQuestionEmbedBuilder(musicIdQuestionStartEvent);
		
		String query;
		switch (question.getPropertyToAsk()) {
		case TITLE:
			query = "What is the title of this track?";
			break;
		case ALBUM:
			query = "Which album did this track come from?";
			break;
		case COMPOSER:
			query = "Who is the composer of this track?";
			break;
		case FEATURED_VOCALIST:
			query = "Who is a featured vocalist of this track?";
			break;
		default:
			query = "What is the " + question.getPropertyToAsk().getFriendlyName() + " of this track?";
			break;
		}
		eb.withTitle("🎵 " + query);
		
		LOG.info(musicIdQuestionStartEvent.toString());
		IMessage message = botUtils.sendEmbedSync(channel, eb.build());
		deletableMessages.add(message);
	}

	@Override
	public void onMusicIdQuestionEnd(MusicIdQuestionEndEvent musicIdQuestionEndEvent) {
		audioManager.stopPlaying();
		
		Win win = musicIdQuestionEndEvent.getWin().orElse(null);
		
		Optional<File> audioCue = win != null ? audioCues.getSuccess() : audioCues.getFailure();
		audioCue.ifPresent(audioManager::playInVC);
		
		List<String> answers = musicIdQuestionEndEvent.getQuestion().getAnswers();

		StringBuilder sb = new StringBuilder();
		
		// The standard "round end" message.
		String stdMessage = getStandardRoundEndMessage(win, answers, null);
		sb.append(stdMessage);
		
		// Add some song metadata to the message.
		sb.append("\n\n");
		SongMetadata sm = musicIdQuestionEndEvent.getSongMetadata();
		MUSIC_ID_QUESTION_END_SONG_FIELDS.stream()
			.forEach(property -> {
				String propertyValue = sm.getProperties().get(property);
				if (propertyValue != null) {
					sb.append(String.format("**%s** %s\n", property.getFriendlyName(), propertyValue));
				}
			});

		IMessage message = botUtils.sendMessageSync(channel, sb.toString());
		deletableMessages.add(message);
	}

	@Override
	public void onAnswerCorrect(AnswerCorrectEvent answerCorrectEvent) {
		IMessage message = discordClient.getMessageByID(answerCorrectEvent.getMessageId());
		if (message != null) {
			react(message, ReactionEmoji.of("✅"));
		}
	}

	@Override
	public void onAnswerIncorrect(AnswerIncorrectEvent answerIncorrectEvent) {
		IMessage message = discordClient.getMessageByID(answerIncorrectEvent.getMessageId());
		if (message != null) {
			react(message, ReactionEmoji.of("❌"));
		}
	}
	
	private void react(IMessage message, ReactionEmoji reactionEmoji) {
		new RequestBuilder(discordClient)
			.shouldBufferRequests(true)
			.setAsync(true)
			.doAction(() -> {
				message.addReaction(reactionEmoji);
				return true;
			})
			.execute();
	}
	
	private void purgeDeletableMessagesAsync() {
		int elementsToTake = deletableMessages.size();
		
		if (elementsToTake > 0) {
			List<IMessage> messagesToDelete = new ArrayList<>();
			for (int i = 0; i < elementsToTake; i++) {
				messagesToDelete.add(deletableMessages.poll());
			}
			
			botUtils.deleteMessagesQuietly(messagesToDelete);
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
	 * @return The sent embed message.
	 */
	private IMessage sendQuestionEmbed(EmbedBuilder eb, QuestionStartEvent<?> qse) {
		File imageFile = qse.getQuestion().getImage().orElse(null);

		if (imageFile != null) {
			String extension = FilenameUtils.getExtension(imageFile.getName());
			String baseName = UUID.randomUUID().toString();
			String discordFriendlyFileName = baseName.replaceAll("[^A-Za-z0-9]", "") + "."
					+ extension;

			eb.withImage("attachment://" + discordFriendlyFileName);

			try {
				FileInputStream fis = new FileInputStream(imageFile);
				return botUtils.sendEmbedSync(channel, eb.build(), fis, discordFriendlyFileName);
			} catch (FileNotFoundException e) {
				// Should never happen because TriviaLibrary performs a file existence check.
				throw new UncheckedIOException(e);
			}

		} else {
			return botUtils.sendEmbedSync(channel, eb.build());
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
	private EmbedBuilder getQuestionEmbedBuilder(QuestionStartEvent<?> qse) {
		EmbedBuilder eb = new EmbedBuilder();

		eb.withColor(accentColor);
		eb.withFooterText(
				String.format("First to correctly answer within %d seconds wins %d points. End your answer with '%s'.",
						qse.getMaxDurationMs() / 1000, 
						qse.getQuestion().getPoints(),
						Constants.ANSWER_SUFFIX));

		eb.withAuthorName(String.format("Question %d of %d", qse.getQuestionNumber(),
				qse.getTotalQuestions()));

		return eb;
	}

	private String getStandardRoundEndMessage(Win win, List<String> correctAnswers, String overrideUserAnswer) {
		String msg;
		if (win == null) {
			StringBuilder sb = new StringBuilder();
			
			sb.append(":alarm_clock: Time's up! ");
			
			if (correctAnswers.size() > 1) {
				sb.append(String.format("The correct answer could be one of: **%s**", StringUtils.join(correctAnswers, ", ")));
			} else {
				sb.append(String.format("The correct answer was: **%s**", correctAnswers.get(0)));
			}
			
			msg = sb.toString();
		} else {
			IUser user = discordClient.fetchUser(win.getWinnerUserId());
			String winningUserMessage = overrideUserAnswer == null ? win.getWinningMessage().getMessage() : overrideUserAnswer;
			
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("**%s** wins %d point(s) for the answer ``%s``",
					user.getDisplayName(channel.getGuild()), win.getPointsAwarded(), winningUserMessage));

			if (win.getIncorrectAttempts() > 0) {
				sb.append(" after ").append(win.getIncorrectAttempts());
				if (win.getIncorrectAttempts() > 1) {
					sb.append(" incorrect attempts");
				} else {
					sb.append(" incorrect attempt");
				}
			}
			sb.append(".");
			
			if (correctAnswers.size() > 1) {
				sb.append(String.format("\n\nAcceptable answers were: %s", StringUtils.join(correctAnswers, ", ")));
			}
			
			msg = sb.toString();
		}

		return msg;
	}

}
