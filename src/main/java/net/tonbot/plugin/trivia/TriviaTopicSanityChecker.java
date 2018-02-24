package net.tonbot.plugin.trivia;

import java.io.File;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.QuestionTemplate;
import net.tonbot.plugin.trivia.model.TriviaTopic;

class TriviaTopicSanityChecker {

	/**
	 * Sanity checks the trivia topic. Specifically, it looks for the following:
	 * <ul>
	 * <li>Image URLs are pointing to files that exist</li>
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

		triviaTopic.getQuestionBundle().getQuestionTemplates().forEach(q -> {
			checkImageUrls(q, triviaTopic, triviaTopicDir);
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
}
