package net.tonbot.plugin.trivia;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

class TriviaSessionManager {

	private static final TriviaConfiguration TRIVIA_CONFIG = TriviaConfiguration.builder()
			.maxQuestions(10)
			.questionTimeSeconds(30)
			.build();

	private final TriviaLibrary triviaLibrary;
	private final Random random;

	private final ConcurrentHashMap<TriviaSessionKey, TriviaSession> sessions;

	@Inject
	public TriviaSessionManager(
			TriviaLibrary triviaLibrary,
			Random random) {
		this.triviaLibrary = Preconditions.checkNotNull(triviaLibrary, "triviaLibrary must be non-null.");
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");

		this.sessions = new ConcurrentHashMap<>();
	}

	public Optional<TriviaSession> getSession(TriviaSessionKey sessionKey) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		return Optional.ofNullable(this.sessions.get(sessionKey));
	}

	/**
	 * Creates a trivia session for the given guild. Any existing sessions will be
	 * replaced, but not before firing their RoundEndEvent.
	 * 
	 * @param sessionKey
	 *            {@link TriviaSessionKey}. Non-null.
	 * @param triviaPackName
	 *            The trivia pack name. Must be the name of a valid trivia pack.
	 *            Non-null.
	 * @return The new {@link TriviaSession}.
	 * @throws InvalidTriviaPackException
	 *             if the specified trivia pack is not valid.
	 */
	public TriviaSession createSession(TriviaSessionKey sessionKey, String triviaPackName, TriviaListener listener) {
		Preconditions.checkNotNull(sessionKey, "sessionKey must be non-null.");
		Preconditions.checkNotNull(triviaPackName, "triviaPackName must be non-null.");

		LoadedTrivia loadedTrivia = triviaLibrary.getTrivia(triviaPackName)
				.orElseThrow(() -> new InvalidTriviaPackException("Trivia pack " + triviaPackName + " is not valid."));
		TriviaSession triviaSession = new TriviaSession(listener, loadedTrivia, TRIVIA_CONFIG, random);
		TriviaSession oldSession = this.sessions.put(sessionKey, triviaSession);

		if (oldSession != null) {
			oldSession.end();
		}

		triviaSession.start();

		return triviaSession;
	}

}
