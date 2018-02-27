package net.tonbot.plugin.trivia;

@SuppressWarnings("serial")
class TriviaTopicSanityException extends RuntimeException {

	public TriviaTopicSanityException(String msg) {
		super(msg);
	}
	
	public TriviaTopicSanityException(String msg, Exception e) {
		super(msg, e);
	}
}
