package net.tonbot.plugin.trivia;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.QuestionBundle;
import net.tonbot.plugin.trivia.model.TriviaMetadata;
import net.tonbot.plugin.trivia.model.TriviaPack;

class TriviaLibrary {

	private static final Logger LOG = LoggerFactory.getLogger(TriviaLibrary.class);

	private final File triviaPacksDir;
	private final ObjectMapper objectMapper;

	private Map<String, TriviaPack> triviaPacks;

	@Inject
	public TriviaLibrary(
			File triviaPacksDir,
			ObjectMapper objectMapper) {
		this.triviaPacksDir = Preconditions.checkNotNull(triviaPacksDir, "triviaPacksDir must be non-null.");
		this.objectMapper = Preconditions.checkNotNull(objectMapper, "objectMapper must be non-null.");
		this.triviaPacks = new HashMap<>();

		scan();
	}

	private void scan() {
		File[] dirs = triviaPacksDir.listFiles(f -> f.isDirectory());
		for (File dir : dirs) {
			try {
				TriviaPack triviaPack = readTriviaPackFromDir(dir);
				this.triviaPacks.put(dir.getName(), triviaPack);
			} catch (CorruptTriviaPackException e) {
				LOG.warn("Trivia pack at {} is corrupt.", e);
			}
		}
	}

	public Optional<TriviaPack> getTriviaPack(String triviaPackName) {
		Preconditions.checkNotNull(triviaPackName, "triviaPackName must be non-null.");

		return Optional.ofNullable(triviaPacks.get(triviaPackName));
	}

	private TriviaPack readTriviaPackFromDir(File triviaPackDir) {
		File metadataFile = new File(triviaPackDir, "metadata.json");
		if (!metadataFile.exists()) {
			throw new CorruptTriviaPackException("metadata.json is missing.");
		}

		File questionsFile = new File(triviaPackDir, "questions.json");
		if (!questionsFile.exists()) {
			throw new CorruptTriviaPackException("questions.json is missing.");
		}

		try {
			TriviaMetadata metadata = objectMapper.readValue(metadataFile, TriviaMetadata.class);
			QuestionBundle questionBundle = objectMapper.readValue(questionsFile, QuestionBundle.class);

			return TriviaPack.builder()
					.metadata(metadata)
					.questionBundle(questionBundle)
					.build();
		} catch (IOException e) {
			throw new CorruptTriviaPackException("Couldn't deserialize objects from trivia pack.", e);
		}
	}
}
