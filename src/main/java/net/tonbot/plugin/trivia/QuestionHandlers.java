package net.tonbot.plugin.trivia;

import java.util.Random;

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
	private final Random random;
	
	@Inject
	public QuestionHandlers(AudioFileIO audioFileIO, Random random) {
		this.audioFileIO = Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
	}
	
	public QuestionHandler get(QuestionTemplate question, TriviaConfiguration config, TriviaListener listener, LoadedTrivia loadedTrivia) {
		Preconditions.checkNotNull(question, "question must be non-null.");
		Preconditions.checkNotNull(config, "config must be non-null.");

		if (question instanceof MultipleChoiceQuestionTemplate) {
			return new MultipleChoiceQuestionHandler((MultipleChoiceQuestionTemplate) question, config, listener);
		} else if (question instanceof ShortAnswerQuestionTemplate) {
			return new ShortAnswerQuestionHandler((ShortAnswerQuestionTemplate) question, listener);
		} else if (question instanceof MusicIdQuestionTemplate) {
			return new MusicIdQuestionHandler((MusicIdQuestionTemplate) question, config, listener, random, audioFileIO, loadedTrivia);
		} else {
			throw new IllegalArgumentException("Unsupported question type: " + question.getClass().getName());
		}
	}
}
