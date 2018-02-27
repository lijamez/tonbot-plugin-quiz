package net.tonbot.plugin.trivia;

import com.google.common.base.Preconditions;

import lombok.Getter;
import net.tonbot.common.TonbotBusinessException;
import sx.blah.discord.handle.obj.IVoiceChannel;

@SuppressWarnings("serial")
class AlreadyInAnotherVoiceChannelException extends TonbotBusinessException {

	@Getter
	private final IVoiceChannel voiceChannel;
	
	public AlreadyInAnotherVoiceChannelException(String message, IVoiceChannel voiceChannel) {
		super(message);
		
		this.voiceChannel = Preconditions.checkNotNull(voiceChannel, "voiceChannel must be non-null.");
	}

}
