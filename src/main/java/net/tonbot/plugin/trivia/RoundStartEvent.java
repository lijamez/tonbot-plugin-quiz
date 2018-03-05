package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.tonbot.plugin.trivia.model.TriviaMetadata;

@Data
@Builder
class RoundStartEvent {

	/**
	 * The trivia that just started.
	 */
	@NonNull
	private final TriviaMetadata triviaMetadata;

	@NonNull
	private final String difficultyName;
	
	private final long startingInMs;
	
	private final boolean hasAudio;
	
	@NonNull
	private final LoadedAudioCues audioCues;
	
	private final File icon;
	
	public Optional<File> getIcon() {
		return Optional.ofNullable(icon);
	}
}
