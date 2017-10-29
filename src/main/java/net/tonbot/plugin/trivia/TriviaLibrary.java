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
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import net.tonbot.plugin.trivia.model.QuestionTemplateBundle;
import net.tonbot.plugin.trivia.model.TriviaMetadata;
import net.tonbot.plugin.trivia.model.TriviaPack;

class TriviaLibrary {

	private static final Logger LOG = LoggerFactory.getLogger(TriviaLibrary.class);

	private final File triviaPacksDir;
	private final ObjectMapper objectMapper;
	private final TriviaPackSanityChecker sanityChecker;

	private Map<String, LoadedTrivia> loadedTrivia;

	@Inject
	public TriviaLibrary(
			File triviaPacksDir,
			ObjectMapper objectMapper,
			TriviaPackSanityChecker sanityChecker) {
		this.triviaPacksDir = Preconditions.checkNotNull(triviaPacksDir, "triviaPacksDir must be non-null.");
		this.objectMapper = Preconditions.checkNotNull(objectMapper, "objectMapper must be non-null.");
		this.sanityChecker = Preconditions.checkNotNull(sanityChecker, "sanityChecker must be non-null.");
		this.loadedTrivia = new HashMap<>();

		scan();
	}

	private void scan() {
		File[] dirs = triviaPacksDir.listFiles(f -> f.isDirectory());
		for (File dir : dirs) {
			try {
				TriviaPack triviaPack = readTriviaPackFromDir(dir);
				LoadedTrivia loadedTrivia = LoadedTrivia.builder()
						.triviaPack(triviaPack)
						.triviaPackDir(dir)
						.build();
				this.loadedTrivia.put(dir.getName(), loadedTrivia);
			} catch (CorruptTriviaPackException e) {
				LOG.warn("Trivia pack at {} is corrupt.", dir.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * Gets an immutable map of trivia pack names to {@link LoadedTrivia}s.
	 */
	public Map<String, LoadedTrivia> getTrivia() {
		return ImmutableMap.copyOf(loadedTrivia);
	}

	/**
	 * Gets a particular trivia pack by name.
	 * 
	 * @param triviaPackName
	 *            Trivia pack name. Non-null.
	 * @return An optional {@link TriviaPack}.
	 */
	public Optional<LoadedTrivia> getTrivia(String triviaPackName) {
		Preconditions.checkNotNull(triviaPackName, "triviaPackName must be non-null.");

		return Optional.ofNullable(loadedTrivia.get(triviaPackName));
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
			QuestionTemplateBundle questionBundle = objectMapper.readValue(questionsFile, QuestionTemplateBundle.class);

			TriviaPack triviaPack = TriviaPack.builder()
					.metadata(metadata)
					.questionBundle(questionBundle)
					.build();

			sanityChecker.check(triviaPack, triviaPackDir);

			return triviaPack;

		} catch (IOException e) {
			throw new CorruptTriviaPackException("Couldn't deserialize objects from trivia pack.", e);
		} catch (TriviaPackSanityException e) {
			throw new CorruptTriviaPackException("Trivia pack has failed sanity checks.", e);
		}
	}
}
