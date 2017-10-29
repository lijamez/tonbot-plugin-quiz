package net.tonbot.plugin.trivia;

import java.io.File;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.tonbot.plugin.trivia.model.TriviaPack;

@Data
@Builder
class LoadedTrivia {

	@NonNull
	private final File triviaPackDir;

	@NonNull
	private final TriviaPack triviaPack;
}
