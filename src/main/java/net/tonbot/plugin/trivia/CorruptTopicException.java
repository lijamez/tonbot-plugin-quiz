package net.tonbot.plugin.trivia;

@SuppressWarnings("serial")
class CorruptTopicException extends RuntimeException {

	public CorruptTopicException(String message) {
		super(message);
	}

	public CorruptTopicException(String message, Exception e) {
		super(message, e);
	}
}
