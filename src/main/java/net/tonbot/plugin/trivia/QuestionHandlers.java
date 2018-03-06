package net.tonbot.plugin.trivia;

import org.jaudiotagger.audio.AudioFileIO;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate;
import net.tonbot.plugin.trivia.model.MusicIdQuestionTemplate;
import net.tonbot.plugin.trivia.model.QuestionTemplate;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionHandler;

class QuestionHandlers {

	private final AudioFileIO audioFileIO;
	private final WeightedRandomPicker randomPicker;
	
	@Inject
	public QuestionHandlers(AudioFileIO audioFileIO, WeightedRandomPicker randomPicker) {
		this.audioFileIO = Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
		this.randomPicker = Preconditions.checkNotNull(randomPicker, "randomPicker must be non-null.");
	}
	
	public QuestionHandler get(QuestionTemplate question, TriviaConfiguration config, TriviaListener listener, LoadedTrivia loadedTrivia) {
		Preconditions.checkNotNull(question, "question must be non-null.");
		Preconditions.checkNotNull(config, "config must be non-null.");

		if (question instanceof MultipleChoiceQuestionTemplate) {
			return new MultipleChoiceQuestionHandler((MultipleChoiceQuestionTemplate) question, config, listener);
		} else if (question instanceof ShortAnswerQuestionTemplate) {
			return new ShortAnswerQuestionHandler((ShortAnswerQuestionTemplate) question, listener, loadedTrivia);
		} else if (question instanceof MusicIdQuestionTemplate) {
			return new MusicIdQuestionHandler((MusicIdQuestionTemplate) question, config, listener, audioFileIO, loadedTrivia, randomPicker);
		} else {
			throw new IllegalArgumentException("Unsupported question type: " + question.getClass().getName());
		}
	}
}
