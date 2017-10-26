package net.tonbot.plugin.quiz;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class TriviaConfiguration {

	private final int maxQuestions;
	private final long questionTimeSeconds;
}
