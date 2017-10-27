package net.tonbot.plugin.trivia;

@SuppressWarnings("serial")
class CorruptTriviaPackException extends RuntimeException {

	public CorruptTriviaPackException(String message) {
		super(message);
	}

	public CorruptTriviaPackException(String message, Exception e) {
		super(message, e);
	}
}
