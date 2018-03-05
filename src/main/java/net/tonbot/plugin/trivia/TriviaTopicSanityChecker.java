package net.tonbot.plugin.trivia;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.KeyNotFoundException;
import org.jaudiotagger.tag.TagException;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.MusicIdQuestionTemplate;
import net.tonbot.plugin.trivia.model.QuestionTemplate;
import net.tonbot.plugin.trivia.model.TriviaTopic;
import net.tonbot.plugin.trivia.musicid.Tag;

class TriviaTopicSanityChecker {

	private final AudioFileIO audioFileIO;
	
	@Inject
	public TriviaTopicSanityChecker(AudioFileIO audioFileIO) {
		this.audioFileIO = Preconditions.checkNotNull(audioFileIO, "audioFileIO must be non-null.");
	}
	
	/**
	 * Sanity checks the trivia topic. Specifically, it looks for the following:
	 * <ul>
	 * <li>Image URLs are pointing to files that exist</li>
	 * <li>Audio paths are pointing to files that exist</li>
	 * </ul>
	 * 
	 * @param triviaTopic
	 *            The trivia topic. Non-null.
	 * @param triviaTopicDir
	 *            The trivia topic's directory. Non-null.
	 * @throws TriviaTopicSanityException
	 *             if the any sanity check failed.
	 */
	public void check(TriviaTopic triviaTopic, File triviaTopicDir) {
		Preconditions.checkNotNull(triviaTopic, "triviaTopic must be non-null.");
		Preconditions.checkNotNull(triviaTopicDir, "triviaTopicDir must be non-null.");
		
		triviaTopic.getMetadata()
			.getAudioCues()
			.ifPresent(audioCues -> {
				audioCues.getSuccessSoundPath().ifPresent(p -> checkFileExistence(p, triviaTopic, triviaTopicDir));
				audioCues.getFailureSoundPath().ifPresent(p -> checkFileExistence(p, triviaTopic, triviaTopicDir));
				audioCues.getRoundCompleteSoundPath().ifPresent(p -> checkFileExistence(p, triviaTopic, triviaTopicDir));
			});
		
		triviaTopic.getMetadata().getIconPath()
			.ifPresent(iconPath -> {
				checkFileExistence(iconPath, triviaTopic, triviaTopicDir);
			});
		
		triviaTopic.getQuestionBundle().getQuestionTemplates().forEach(q -> {
				checkImageUrls(q, triviaTopic, triviaTopicDir);
				checkMusicIdQuestions(q, triviaTopic, triviaTopicDir);
			});
	}

	private void checkImageUrls(QuestionTemplate q, TriviaTopic triviaTopic, File triviaTopicDir) {
		q.getImagePaths().forEach(imgUrl -> {
			File imgFile = new File(triviaTopicDir, imgUrl);
			if (!imgFile.exists()) {
				throw new TriviaTopicSanityException("Trivia Topic " + triviaTopic.getMetadata().getName()
						+ " uses an non-existent image at " + imgUrl);
			}
		});
	}
	
	private void checkMusicIdQuestions(QuestionTemplate q, TriviaTopic triviaTopic, File triviaTopicDir) {
		if (q instanceof MusicIdQuestionTemplate) {
			MusicIdQuestionTemplate musicIdQuestionTemplate = ((MusicIdQuestionTemplate) q);
			
			checkAudioFileExistence(musicIdQuestionTemplate, triviaTopic, triviaTopicDir);
			checkAudioFileHasAllRequiredTags(musicIdQuestionTemplate, triviaTopic, triviaTopicDir);
		}
	}
	
	private void checkAudioFileExistence(MusicIdQuestionTemplate q, TriviaTopic triviaTopic, File triviaTopicDir) {
		String audioRelativePath = q.getAudioPath();
		checkFileExistence(audioRelativePath, triviaTopic, triviaTopicDir);
	}
	
	private void checkFileExistence(String relativePath, TriviaTopic triviaTopic, File triviaTopicDir) {
		File audioFile = new File(triviaTopicDir, relativePath);
		if (!audioFile.exists()) {
			throw new TriviaTopicSanityException("Trivia Topic " + triviaTopic.getMetadata().getName()
					+ " uses an non-existent file at " + relativePath);
		}
	}
	
	private void checkAudioFileHasAllRequiredTags(MusicIdQuestionTemplate q, TriviaTopic triviaTopic, File triviaTopicDir) {
		AudioFile audioFile;
		try {
			audioFile = audioFileIO.readFile(new File(triviaTopicDir, q.getAudioPath()));
		} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
			throw new TriviaTopicSanityException("Unable to perform sanity check on a music ID question with audio file " + q.getAudioPath(), e);
		}
		
		for (Tag tag : q.getTags()) {
			List<String> tagValues = ImmutableList.of();
			try {
				tagValues = audioFile.getTag()
						.getAll(tag.getFieldKey());
			} catch (KeyNotFoundException e) { }
			
			if (tagValues.isEmpty()) {
				throw new TriviaTopicSanityException("Trivia Topic " + triviaTopic.getMetadata().getName()
						+ " has a question which asks of " + tag + " for " + q.getAudioPath() + " but the file doesn't have that tag.");
			}
		}
	}
}
