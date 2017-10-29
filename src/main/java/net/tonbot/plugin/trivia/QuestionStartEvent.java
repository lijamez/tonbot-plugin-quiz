package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.Optional;

import com.google.common.base.Preconditions;

import lombok.Data;
import net.tonbot.plugin.trivia.model.QuestionTemplate;

@Data
abstract class QuestionStartEvent {

	private final QuestionTemplate question;

	private final long questionNumber;
	private final long totalQuestions;

	private final long maxDurationSeconds;

	private final File image;

	public QuestionStartEvent(
			QuestionTemplate question,
			long questionNumber,
			long totalQuestions,
			long maxDurationSeconds,
			File image) {
		Preconditions.checkArgument(questionNumber > 0, "questionNumber must be positive.");
		Preconditions.checkArgument(totalQuestions > 0, "totalQuestions must be positive.");
		Preconditions.checkArgument(maxDurationSeconds > 0, "maxDurationSeconds must be positive.");

		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.questionNumber = questionNumber;
		this.totalQuestions = totalQuestions;
		this.maxDurationSeconds = maxDurationSeconds;
		this.image = image;
	}

	public Optional<File> getImage() {
		return Optional.ofNullable(image);
	}
}
