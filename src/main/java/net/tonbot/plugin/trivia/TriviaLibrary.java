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
import net.tonbot.plugin.trivia.model.TriviaTopic;

class TriviaLibrary {

	private static final Logger LOG = LoggerFactory.getLogger(TriviaLibrary.class);

	private final File triviaTopicsDir;
	private final ObjectMapper objectMapper;
	private final TriviaTopicSanityChecker sanityChecker;

	private Map<String, LoadedTrivia> loadedTrivia;

	@Inject
	public TriviaLibrary(File triviaTopicsDir, ObjectMapper objectMapper, TriviaTopicSanityChecker sanityChecker) {
		this.triviaTopicsDir = Preconditions.checkNotNull(triviaTopicsDir, "triviaTopicsDir must be non-null.");
		this.objectMapper = Preconditions.checkNotNull(objectMapper, "objectMapper must be non-null.");
		this.sanityChecker = Preconditions.checkNotNull(sanityChecker, "sanityChecker must be non-null.");
		this.loadedTrivia = new HashMap<>();

		scan();
	}

	private void scan() {
		File[] dirs = triviaTopicsDir.listFiles(f -> f.isDirectory());
		for (File dir : dirs) {
			try {
				TriviaTopic triviaTopic = readTriviaTopicFromDir(dir);
				LoadedTrivia loadedTrivia = LoadedTrivia.builder()
						.triviaTopic(triviaTopic)
						.triviaTopicDir(dir)
						.build();
				this.loadedTrivia.put(dir.getName(), loadedTrivia);
			} catch (CorruptTopicException e) {
				LOG.warn("Trivia topic at {} is corrupt.", dir.getAbsolutePath(), e);
			}
		}
	}

	/**
	 * Gets an immutable map of trivia topic names to {@link LoadedTrivia}s.
	 */
	public Map<String, LoadedTrivia> getTrivia() {
		return ImmutableMap.copyOf(loadedTrivia);
	}

	/**
	 * Gets a particular trivia topic by name.
	 * 
	 * @param triviaTopicName
	 *            Trivia topic name. Non-null.
	 * @return An optional {@link TriviaTopic}.
	 */
	public Optional<LoadedTrivia> getTrivia(String triviaTopicName) {
		Preconditions.checkNotNull(triviaTopicName, "triviaTopicName must be non-null.");

		return Optional.ofNullable(loadedTrivia.get(triviaTopicName));
	}

	private TriviaTopic readTriviaTopicFromDir(File triviaTopicDir) {
		File metadataFile = new File(triviaTopicDir, "metadata.json");
		if (!metadataFile.exists()) {
			throw new CorruptTopicException("metadata.json is missing.");
		}

		File questionsFile = new File(triviaTopicDir, "questions.json");
		if (!questionsFile.exists()) {
			throw new CorruptTopicException("questions.json is missing.");
		}

		try {
			TriviaMetadata metadata = objectMapper.readValue(metadataFile, TriviaMetadata.class);
			QuestionTemplateBundle questionBundle = objectMapper.readValue(questionsFile, QuestionTemplateBundle.class);

			TriviaTopic triviaTopic = TriviaTopic.builder().metadata(metadata).questionBundle(questionBundle).build();

			sanityChecker.check(triviaTopic, triviaTopicDir);

			return triviaTopic;

		} catch (IOException e) {
			throw new CorruptTopicException("Couldn't deserialize objects from trivia topic.", e);
		} catch (TriviaTopicSanityException e) {
			throw new CorruptTopicException("Trivia topic has failed sanity checks.", e);
		}
	}
}
