package net.tonbot.plugin.trivia;

import java.io.File;

import com.google.common.base.Preconditions;

import net.tonbot.plugin.trivia.model.QuestionTemplate;
import net.tonbot.plugin.trivia.model.TriviaPack;

class TriviaPackSanityChecker {

	/**
	 * Sanity checks the trivia pack. Specifically, it looks for the following:
	 * <ul>
	 * <li>Image URLs are pointing to files that exist</li>
	 * </ul>
	 * 
	 * @param triviaPack
	 *            The trivia pack. Non-null.
	 * @param triviaPackDir
	 *            The trivia pack's directory. Non-null.
	 * @throws TriviaPackSanityException
	 *             if the any sanity check failed.
	 */
	public void check(TriviaPack triviaPack, File triviaPackDir) {
		Preconditions.checkNotNull(triviaPack, "triviaPack must be non-null.");
		Preconditions.checkNotNull(triviaPackDir, "triviaPackDir must be non-null.");

		triviaPack.getQuestionBundle().getQuestionTemplates().forEach(q -> {
			checkImageUrls(q, triviaPack, triviaPackDir);
		});
	}

	private void checkImageUrls(QuestionTemplate q, TriviaPack triviaPack, File triviaPackDir) {
		q.getImagePaths().forEach(imgUrl -> {
			File imgFile = new File(triviaPackDir, imgUrl);
			if (!imgFile.exists()) {
				throw new TriviaPackSanityException("Trivia Pack " + triviaPack.getMetadata().getName()
						+ " uses an non-existent image at " + imgUrl);
			}
		});
	}
}
