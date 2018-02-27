package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.Optional;

import com.google.common.base.Preconditions;

import lombok.Data;
import net.tonbot.plugin.trivia.model.QuestionTemplate;

@Data
public abstract class QuestionStartEvent {

	private final QuestionTemplate question;

	private final long questionNumber;
	private final long totalQuestions;

	private final long maxDurationMs;

	private final File image;

	public QuestionStartEvent(QuestionTemplate question, long questionNumber, long totalQuestions,
			long maxDurationMs, File image) {
		Preconditions.checkArgument(questionNumber > 0, "questionNumber must be positive.");
		Preconditions.checkArgument(totalQuestions > 0, "totalQuestions must be positive.");
		Preconditions.checkArgument(maxDurationMs > 0, "maxDurationMs must be positive.");

		this.question = Preconditions.checkNotNull(question, "question must be non-null.");
		this.questionNumber = questionNumber;
		this.totalQuestions = totalQuestions;
		this.maxDurationMs = maxDurationMs;
		this.image = image;
	}

	public Optional<File> getImage() {
		return Optional.ofNullable(image);
	}
}
