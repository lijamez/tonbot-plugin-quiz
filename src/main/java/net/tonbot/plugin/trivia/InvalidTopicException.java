package net.tonbot.plugin.trivia;

@SuppressWarnings("serial")
class InvalidTopicException extends RuntimeException {

	public InvalidTopicException(String message) {
		super(message);
	}
}
