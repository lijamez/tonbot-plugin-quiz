package net.tonbot.plugin.trivia.model;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class TriviaPack {

	@NonNull
	private final TriviaMetadata metadata;

	@NonNull
	private final QuestionBundle questionBundle;
}
