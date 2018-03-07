package net.tonbot.plugin.trivia.musicid;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import lombok.Data;
import lombok.NonNull;
import net.tonbot.plugin.trivia.LoadedTrivia;
import net.tonbot.plugin.trivia.QuestionGenerator;
import net.tonbot.plugin.trivia.TriviaConfiguration;
import net.tonbot.plugin.trivia.WeightedRandomPicker;
import net.tonbot.plugin.trivia.model.MusicIdQuestionTemplate;
import net.tonbot.plugin.trivia.model.SongPropertyData;

public class MusicIdQuestionGenerator implements QuestionGenerator<MusicIdQuestionTemplate, MusicIdQuestion>{
	
	private final AudioFileIO audioFileIO;
	private final WeightedRandomPicker randomPicker;
	
	@Inject
	public MusicIdQuestionGenerator(AudioFileIO audioFileIO, WeightedRandomPicker randomPicker) {
		this.randomPicker = Preconditions.checkNotNull(randomPicker, "randomPicker must be non-null.");
		this.audioFileIO = Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
	}
	
	@Override
	public MusicIdQuestion generate(LoadedTrivia loadedTrivia, TriviaConfiguration config, MusicIdQuestionTemplate questionTemplate) {
		Preconditions.checkNotNull(loadedTrivia, "loadedTrivia must be non-null.");
		Preconditions.checkNotNull(questionTemplate, "questionTemplate must be non-null.");
		
		AudioFile audioFile = getAudioFile(questionTemplate, loadedTrivia);
		
		PropertyValues propertyValues = getRandomUsableTagValues(questionTemplate, audioFile, loadedTrivia);
		
		return MusicIdQuestion.builder()
				.audioFile(audioFile.getFile())
				.propertyToAsk(propertyValues.getProperty())
				.answers(propertyValues.getAnswers())
				.points(questionTemplate.getPoints())
				.build();
		
	}
	
	private PropertyValues getRandomUsableTagValues(MusicIdQuestionTemplate qt, AudioFile audioFile, LoadedTrivia loadedTrivia) {
		
		Map<SongProperty, Long> propertyWeights = loadedTrivia.getTriviaTopic().getMetadata().getSongPropertyWeights();
		
		Map<SongProperty, Long> qtPropertyWeights = new HashMap<>();
		
		for (SongProperty sp : qt.getProperties().keySet()) {
			qtPropertyWeights.put(sp, propertyWeights.getOrDefault(sp, null));
		}
		
		SongProperty propertyToAsk = randomPicker.pick(qtPropertyWeights);
		
		List<String> answers = getAnswers(propertyToAsk, qt, audioFile);
		
		if (answers.isEmpty()) {
			throw new IllegalStateException("Unable to find any answers for property " + propertyToAsk 
					+ "  for audio file " + audioFile.getFile().getAbsolutePath());	
		}
		
		return new PropertyValues(propertyToAsk, answers);
	}
	
	private List<String> getAnswers(SongProperty propertyToAsk, MusicIdQuestionTemplate qt, AudioFile audioFile) {
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
	
	private AudioFile getAudioFile(MusicIdQuestionTemplate questionTemplate, LoadedTrivia loadedTrivia) {
		try {
			AudioFile audioFile = audioFileIO.readFile(new File(loadedTrivia.getTriviaTopicDir(), questionTemplate.getAudioPath()));
			return audioFile;
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException
				| InvalidAudioFrameException e) {
			throw new IllegalStateException("Unable to read audio file at " + questionTemplate.getAudioPath(), e);
		}
	}
	
	@Data
	private static class PropertyValues {
		@NonNull
		private final SongProperty property;
		
		@NonNull
		private final List<String> answers;
	}
}
