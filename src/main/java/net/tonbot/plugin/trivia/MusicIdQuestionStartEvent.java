package net.tonbot.plugin.trivia;

import java.io.File;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.MusicIdentificationQuestionTemplate;
import net.tonbot.plugin.trivia.model.TrackProperty;

@Data
@EqualsAndHashCode(callSuper = true)
class MusicIdQuestionStartEvent extends QuestionStartEvent {

	private final File track;
	private final TrackProperty trackProperty;

	/**
	 * @param miQuestion
	 *            {@link MusicIdentificationQuestionTemplate}. Non-null.
	 * @param questionNumber
	 *            Question number.
	 * @param totalQuestions
	 *            The total number of questions expected to be asked.
	 * @param maxDurationSeconds
	 *            The max amount of time that this question will be open for.
	 * @param imageFile
	 *            Image file. Nullable.
	 * @param track
	 *            The audio track to be played. Non-null.
	 * @param trackProperty
	 *            A property of the track to ask for. Non-null.
	 */
	@Builder
	private MusicIdQuestionStartEvent(
			MusicIdentificationQuestionTemplate miQuestion,
			long questionNumber,
			long totalQuestions,
			long maxDurationSeconds,
			File imageFile,
			File track,
			TrackProperty trackProperty) {
		super(miQuestion, questionNumber, totalQuestions, maxDurationSeconds, imageFile);

		this.track = Preconditions.checkNotNull(track, "track must be non-null.");
		this.trackProperty = Preconditions.checkNotNull(trackProperty, "trackProperty must be non-null.");
	}
}
