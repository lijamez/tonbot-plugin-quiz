package net.tonbot.plugin.trivia;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import lombok.Getter;

public class SingleTrackLoadResultHandler implements AudioLoadResultHandler {

	@Getter
	public AudioTrack audioTrack;
	
	@Override
	public void trackLoaded(AudioTrack track) {
		audioTrack = track;
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		throw new IllegalStateException("Lavaplayer has unexpectedly loaded a playlist.");
	}

	@Override
	public void noMatches() {
		throw new IllegalStateException("Lavaplayer could not find a track to play.");
		
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		throw new IllegalStateException("An error occurred when loading the track.", exception);
	}
}
