package net.tonbot.plugin.trivia.musicid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Data;
import lombok.NonNull;
import net.tonbot.plugin.trivia.FuzzyMatcher;
import net.tonbot.plugin.trivia.LoadedTrivia;
import net.tonbot.plugin.trivia.QuestionHandler;
import net.tonbot.plugin.trivia.TriviaConfiguration;
import net.tonbot.plugin.trivia.TriviaListener;
import net.tonbot.plugin.trivia.UserMessage;
import net.tonbot.plugin.trivia.Win;
import net.tonbot.plugin.trivia.model.MusicIdQuestionTemplate;
import net.tonbot.plugin.trivia.model.SongPropertyData;

public class MusicIdQuestionHandler implements QuestionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MusicIdQuestionHandler.class);
	
	private final MusicIdQuestionTemplate questionTemplate;
	private final TriviaListener listener;
	private final FuzzyMatcher fuzzyMatcher;
	private final PropertyValues askingTagValues;
	private final AudioFile audioFile;
	
	public MusicIdQuestionHandler(
			MusicIdQuestionTemplate questionTemplate, 
			TriviaConfiguration config,
			TriviaListener listener,
			AudioFileIO audioFileIO, 
			LoadedTrivia loadedTrivia) {
		this.questionTemplate = Preconditions.checkNotNull(questionTemplate, "questionTemplate must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
		Preconditions.checkNotNull(loadedTrivia, "loadedTrivia must be non-null.");
		this.fuzzyMatcher = new FuzzyMatcher(loadedTrivia.getTriviaTopic().getMetadata().getSynonyms());
		this.audioFile = getAudioFile(audioFileIO, questionTemplate, loadedTrivia);
		this.askingTagValues = getRandomUsableTagValues(questionTemplate, audioFile);
	}
	
	private PropertyValues getRandomUsableTagValues(MusicIdQuestionTemplate qt, AudioFile audioFile) {
		
		List<SongProperty> properties = new ArrayList<>(qt.getProperties().keySet());
		Collections.shuffle(properties);
		
		for (SongProperty propertyToAsk : properties) {
			List<String> answers = getAnswers(propertyToAsk, qt);
			
			if (answers.isEmpty()) {
				LOG.warn("The question with audio file " + audioFile.getFile().getAbsolutePath() 
						+ " doesn't contain any possible answers for property " + propertyToAsk);
				continue;
			}
			
			return new PropertyValues(propertyToAsk, answers);
		}
		
		throw new IllegalStateException("Unable to find a suitable property to ask for audio file " + audioFile.getFile().getAbsolutePath() 
				+ ". These have been tried: " + qt.getProperties().keySet());

	}
	
	List<String> getAnswers(SongProperty propertyToAsk, MusicIdQuestionTemplate qt) {
		SongPropertyData tagData = qt.getProperties().get(propertyToAsk);
		
		List<String> answers = tagData.getAnswers()
			.orElseGet(() -> {
				List<String> propertyValues = propertyToAsk.getFieldKey()
						.map(fieldKey -> audioFile.getTag().getAll(fieldKey))
						.orElse(ImmutableList.of());
				
				return propertyValues;
			});
		
		return answers;
	}
	
	private AudioFile getAudioFile(AudioFileIO audioFileIO, MusicIdQuestionTemplate questionTemplate, LoadedTrivia loadedTrivia) {
		try {
			AudioFile audioFile = audioFileIO.readFile(new File(loadedTrivia.getTriviaTopicDir(), questionTemplate.getAudioPath()));
			return audioFile;
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			throw new IllegalStateException("Unable to read audio file at " + questionTemplate.getAudioPath(), e);
		}
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationMs, File imageFile) {
		MusicIdQuestionStartEvent event = MusicIdQuestionStartEvent.builder()
				.image(imageFile)
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.maxDurationMs(maxDurationMs)
				.question(questionTemplate)
				.propertyToAsk(askingTagValues.getProperty())
				.audioFile(audioFile.getFile())
				.build();
				
		listener.onMusicIdQuestionStart(event);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		String answer = userMessage.getMessage();
		
		boolean answerIsCorrect = fuzzyMatcher.matches(answer, askingTagValues.getAnswers());
		
		return Optional.of(answerIsCorrect);
	}

	@Override
	public void notifyEnd(UserMessage userMessage, long awardedPoints, long incorrectAttempts) {
		
		Win win = null;
		if (userMessage != null) {
			win = Win.builder()
					.incorrectAttempts(incorrectAttempts)
					.pointsAwarded(awardedPoints)
					.winningMessage(userMessage)
					.build();
		}
		
		SongMetadata songMetadata = getSongMetadataForEvent(audioFile);
		
		MusicIdQuestionEndEvent event = MusicIdQuestionEndEvent.builder()
				.property(askingTagValues.getProperty())
				.answers(askingTagValues.getAnswers())
				.timedOut(userMessage == null)
				.win(win)
				.songMetadata(songMetadata)
				.build();
		
		listener.onMusicIdQuestionEnd(event);
	}
	
	private SongMetadata getSongMetadataForEvent(AudioFile af) {
		Map<SongProperty, String> properties = Arrays.asList(SongProperty.values()).stream()
				.filter(tag -> tag.getFieldKey().isPresent())
				.filter(tag -> !StringUtils.isBlank(af.getTag().getFirst(tag.getFieldKey().get())))
				.collect(Collectors.toMap(t -> t, t -> af.getTag().getFirst(t.getFieldKey().get())));
		
		SongMetadata sm = SongMetadata.builder()
				.properties(properties)
				.build();
		
		return sm;
	}

	@Data
	private static class PropertyValues {
		@NonNull
		private final SongProperty property;
		
		@NonNull
		private final List<String> answers;
	}
}
