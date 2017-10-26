package net.tonbot.plugin.quiz;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.tonbot.plugin.quiz.model.TriviaMetadata;

@Data
@Builder
class RoundStartEvent {
	
	/**
	 * The trivia that just started.
	 */
	@NonNull
	private final TriviaMetadata triviaMetadata;
}
