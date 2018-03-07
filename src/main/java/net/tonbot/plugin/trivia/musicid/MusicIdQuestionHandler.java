package net.tonbot.plugin.trivia.musicid;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
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

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.FuzzyMatcher;
import net.tonbot.plugin.trivia.LoadedTrivia;
import net.tonbot.plugin.trivia.QuestionHandler;
import net.tonbot.plugin.trivia.TriviaConfiguration;
import net.tonbot.plugin.trivia.TriviaListener;
import net.tonbot.plugin.trivia.UserMessage;
import net.tonbot.plugin.trivia.Win;
import net.tonbot.plugin.trivia.model.Question;

public class MusicIdQuestionHandler implements QuestionHandler {
	
	private final MusicIdQuestion question;
	private final TriviaListener listener;
	private final FuzzyMatcher fuzzyMatcher;
	private final AudioFileIO audioFileIO;
	
	public MusicIdQuestionHandler(
			MusicIdQuestion question, 
			TriviaConfiguration config,
			TriviaListener listener,
			LoadedTrivia loadedTrivia,
			AudioFileIO audioFileIO) {
		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.listener = Preconditions.checkNotNull(listener, "listener must be non-null.");
		Preconditions.checkNotNull(loadedTrivia, "loadedTrivia must be non-null.");
		this.fuzzyMatcher = new FuzzyMatcher(loadedTrivia.getTriviaTopic().getMetadata().getSynonyms());
		this.audioFileIO = Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
	}

	@Override
	public Question getQuestion() {
		return question;
	}
	
	@Override
	public void notifyStart(long questionNumber, long totalQuestions, long maxDurationMs) {
		MusicIdQuestionStartEvent event = MusicIdQuestionStartEvent.builder()
				.questionNumber(questionNumber)
				.totalQuestions(totalQuestions)
				.maxDurationMs(maxDurationMs)
				.question(question)
				.build();
				
		listener.onMusicIdQuestionStart(event);
	}

	@Override
	public Optional<Boolean> checkCorrectness(UserMessage userMessage) {
		String answer = userMessage.getMessage();
		
		boolean answerIsCorrect = fuzzyMatcher.matches(answer, question.getAnswers());
		
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
		
		SongMetadata songMetadata = getSongMetadataForEvent(question.getAudioFile());
		
		MusicIdQuestionEndEvent event = MusicIdQuestionEndEvent.builder()
				.question(question)
				.timedOut(userMessage == null)
				.win(win)
				.songMetadata(songMetadata)
				.build();
		
		listener.onMusicIdQuestionEnd(event);
	}
	
	private SongMetadata getSongMetadataForEvent(File file) {
		
		try {
			AudioFile af = audioFileIO.readFile(file);
			
			Map<SongProperty, String> properties = Arrays.asList(SongProperty.values()).stream()
					.filter(tag -> tag.getFieldKey().isPresent())
					.filter(tag -> !StringUtils.isBlank(af.getTag().getFirst(tag.getFieldKey().get())))
					.collect(Collectors.toMap(t -> t, t -> af.getTag().getFirst(t.getFieldKey().get())));
			
			SongMetadata sm = SongMetadata.builder()
					.properties(properties)
					.build();
			
			return sm;
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			throw new IllegalStateException("Unable to read metadata from file " + file.getAbsolutePath(), e);
		}
	}
}
