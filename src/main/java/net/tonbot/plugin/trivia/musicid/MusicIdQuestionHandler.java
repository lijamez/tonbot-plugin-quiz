package net.tonbot.plugin.trivia.musicid;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

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

public class MusicIdQuestionHandler implements QuestionHandler {

	private static final Logger LOG = LoggerFactory.getLogger(MusicIdQuestionHandler.class);
	
	private final MusicIdQuestionTemplate questionTemplate;
	private final TriviaListener listener;
	private final TagValues tagToAsk;
	private final AudioFile audioFile;
	
	public MusicIdQuestionHandler(MusicIdQuestionTemplate questionTemplate, 
			TriviaConfiguration config,
			TriviaListener listener,
			Random random,
			AudioFileIO audioFileIO, 
			LoadedTrivia loadedTrivia) {
		this.questionTemplate = Preconditions.checkNotNull(questionTemplate, "questionTemplate must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		Preconditions.checkNotNull(random, "random must be non-null.");
		Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
		Preconditions.checkNotNull(loadedTrivia, "loadedTrivia must be non-null.");
		
		this.audioFile = getAudioFile(audioFileIO, questionTemplate, loadedTrivia);
		this.tagToAsk = getRandomUsableTagValues(audioFile, questionTemplate.getTags())
				.orElseThrow(() -> new IllegalArgumentException("The audio file " + audioFile.getFile().getAbsolutePath() 
						+ " doesn't contain any of the tags " + questionTemplate.getTags()));
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
	
	private Optional<TagValues> getRandomUsableTagValues(AudioFile audioFile, Collection<Tag> possibleTags) {
		
		List<Tag> remainingTags = new ArrayList<>(possibleTags);
		Collections.shuffle(remainingTags);
		
		for (Tag tag : remainingTags) {
			try {
				List<String> tagValues = audioFile.getTag()
						.getAll(tag.getFieldKey());
				
				if (!tagValues.isEmpty()) {
					return Optional.of(new TagValues(tag, tagValues));
				}
			} catch (KeyNotFoundException e) { }
			
			LOG.warn("The audio file {} has no {} tag.", audioFile.getFile().getAbsolutePath(), tag);
		}
		
		return Optional.empty();
	}

	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationMs, File imageFile) {
		MusicIdQuestionStartEvent event = MusicIdQuestionStartEvent.builder()
				.image(imageFile)
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.maxDurationMs(maxDurationMs)
				.question(questionTemplate)
				.tagToAsk(tagToAsk.getTag())
				.audioFile(audioFile.getFile())
				.build();
				
		listener.onMusicIdQuestionStart(event);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		String answer = userMessage.getMessage();
		
		boolean answerIsCorrect = FuzzyMatcher.matches(answer, tagToAsk.getValues());
		
		return Optional.of(answerIsCorrect);
	}

	@Override
	public void notifyEnd(UserMessage userMessage, long awardedPoints, long incorrectAttempts) {
		String canonicalAnswer = audioFile.getTag().getFirst(tagToAsk.getTag().getFieldKey());
		
		Win win = null;
		if (userMessage != null) {
			win = Win.builder()
					.incorrectAttempts(incorrectAttempts)
					.pointsAwarded(awardedPoints)
					.winningMessage(userMessage)
					.build();
		}
		
		MusicIdQuestionEndEvent event = MusicIdQuestionEndEvent.builder()
				.canonicalAnswer(canonicalAnswer)
				.timedOut(userMessage == null)
				.win(win)
				.build();
		
		listener.onMusicIdQuestionEnd(event);
	}

	@Data
	private static class TagValues {
		@NonNull
		private final Tag tag;
		
		@NonNull
		private final List<String> values;
	}
}
