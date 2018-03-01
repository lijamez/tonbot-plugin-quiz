package net.tonbot.plugin.trivia;

import net.tonbot.common.TonbotBusinessException;

@SuppressWarnings("serial")
class ExistingSessionException extends TonbotBusinessException {

	public ExistingSessionException(String message) {
		super(message);
	}

}
