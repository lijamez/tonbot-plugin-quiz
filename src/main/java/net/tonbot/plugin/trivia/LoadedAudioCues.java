package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class LoadedAudioCues {
	
	private final File success;
	private final File failure;
	private final File roundStart;
	private final File roundComplete;
	
	public Optional<File> getSuccess() {
		return Optional.ofNullable(success);
	}
	
	public Optional<File> getFailure() {
		return Optional.ofNullable(failure);
	}
	
	public Optional<File> getRoundStart() {
		return Optional.ofNullable(roundStart);
	}
	
	public Optional<File> getRoundComplete() {
		return Optional.ofNullable(roundComplete);
	}
}
