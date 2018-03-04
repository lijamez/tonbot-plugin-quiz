package net.tonbot.plugin.trivia.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class AudioCues {

	private final String successSoundPath;
	private final String failureSoundPath;
	private final String roundStartSoundPath;
	private final String roundCompleteSoundPath;
	
	@JsonCreator
	public AudioCues(
			@JsonProperty("success") String successSoundPath,
			@JsonProperty("failure") String failureSoundPath,
			@JsonProperty("roundStart") String roundStartSoundPath,
			@JsonProperty("roundComplete") String roundCompleteSoundPath) {
		this.successSoundPath = successSoundPath;
		this.failureSoundPath = failureSoundPath;
		this.roundStartSoundPath = roundStartSoundPath;
		this.roundCompleteSoundPath = roundCompleteSoundPath;
	}
	
	public Optional<String> getSuccessSoundPath() {
		return Optional.ofNullable(successSoundPath);
	}
	
	public Optional<String> getFailureSoundPath() {
		return Optional.ofNullable(failureSoundPath);
	}
	
	public Optional<String> getRoundStartSoundPath() {
		return Optional.ofNullable(roundStartSoundPath);
	}
	
	public Optional<String> getRoundCompleteSoundPath() {
		return Optional.ofNullable(roundCompleteSoundPath);
	}
}
