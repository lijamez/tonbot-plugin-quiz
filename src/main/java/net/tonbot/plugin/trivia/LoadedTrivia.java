package net.tonbot.plugin.trivia;

import java.io.File;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import net.tonbot.plugin.trivia.model.TriviaTopic;

@Data
@Builder
public class LoadedTrivia {

	@NonNull
	private final File triviaTopicDir;

	@NonNull
	private final TriviaTopic triviaTopic;
}
