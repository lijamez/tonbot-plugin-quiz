package net.tonbot.plugin.trivia;

import org.jaudiotagger.audio.AudioFileIO;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate;
import net.tonbot.plugin.trivia.model.MusicIdQuestionTemplate;
import net.tonbot.plugin.trivia.model.QuestionTemplate;
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate;
import net.tonbot.plugin.trivia.multiplechoice.MultipleChoiceQuestion;
import net.tonbot.plugin.trivia.multiplechoice.MultipleChoiceQuestionGenerator;
import net.tonbot.plugin.trivia.multiplechoice.MultipleChoiceQuestionHandler;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestion;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionGenerator;
import net.tonbot.plugin.trivia.musicid.MusicIdQuestionHandler;
import net.tonbot.plugin.trivia.shortanswer.ShortAnswerQuestion;
import net.tonbot.plugin.trivia.shortanswer.ShortAnswerQuestionGenerator;
import net.tonbot.plugin.trivia.shortanswer.ShortAnswerQuestionHandler;

class QuestionHandlers {

	private final ShortAnswerQuestionGenerator shortAnswerQuestionGenerator;
	private final MultipleChoiceQuestionGenerator multipleChoiceQuestionGenerator;
	private final MusicIdQuestionGenerator musicIdQuestionGenerator;
	
	private final AudioFileIO audioFileIO;
	
	@Inject
	public QuestionHandlers(
			ShortAnswerQuestionGenerator shortAnswerQuestionGenerator,
			MultipleChoiceQuestionGenerator multipleChoiceQuestionGenerator,
			MusicIdQuestionGenerator musicIdQuestionGenerator,
			AudioFileIO audioFileIO) {
		this.shortAnswerQuestionGenerator = Preconditions.checkNotNull(shortAnswerQuestionGenerator, "shortAnswerQuestionGenerator must be non-null.");
		this.multipleChoiceQuestionGenerator = Preconditions.checkNotNull(multipleChoiceQuestionGenerator, "multipleChoiceQuestionGenerator must be non-null.");
		this.musicIdQuestionGenerator = Preconditions.checkNotNull(musicIdQuestionGenerator, "musicIdQuestionGenerator must be non-null.");
		
		this.audioFileIO = Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
	}
	
	public QuestionHandler get(QuestionTemplate questionTemplate, TriviaConfiguration config, TriviaListener listener, LoadedTrivia loadedTrivia) {
		Preconditions.checkNotNull(questionTemplate, "questionTemplate must be non-null.");
		Preconditions.checkNotNull(config, "config must be non-null.");

		if (questionTemplate instanceof MultipleChoiceQuestionTemplate) {
			MultipleChoiceQuestion question = multipleChoiceQuestionGenerator.generate(loadedTrivia, config, (MultipleChoiceQuestionTemplate) questionTemplate);
			return new MultipleChoiceQuestionHandler(question, config, listener);
		} else if (questionTemplate instanceof ShortAnswerQuestionTemplate) {
			ShortAnswerQuestion question = shortAnswerQuestionGenerator.generate(loadedTrivia, config, (ShortAnswerQuestionTemplate) questionTemplate);
			return new ShortAnswerQuestionHandler(question, listener, loadedTrivia);
		} else if (questionTemplate instanceof MusicIdQuestionTemplate) {
			MusicIdQuestion question = musicIdQuestionGenerator.generate(loadedTrivia, config, (MusicIdQuestionTemplate) questionTemplate);
			return new MusicIdQuestionHandler(question, config, listener, loadedTrivia, audioFileIO);
		} else {
			throw new IllegalArgumentException("Unsupported question template type: " + questionTemplate.getClass().getName());
		}
	}
}
