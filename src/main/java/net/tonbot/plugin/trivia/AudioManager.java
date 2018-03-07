package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Preconditions;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.tonbot.common.TonbotTechnicalFault;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;

class AudioManager {

	private final IGuild guild;
	private final AudioPlayerManager audioPlayerManager;
	
	private AudioPlayer audioPlayer;
	
	public AudioManager(IGuild guild, AudioPlayerManager audioPlayerManager) {
		this.guild = Preconditions.checkNotNull(guild, "guild must be non-null.");
		this.audioPlayerManager = Preconditions.checkNotNull(audioPlayerManager, "audioPlayerManager must be non-null.");
	}
	
	/**
	 * Connect to the given voice channel.
	 * @param voiceChannel The voice channel to join. Non-null.
	 * @throws MissingPermissionsException if there are no permissions to connect to that voice channel.
	 * @throws AlreadyInAnotherVoiceChannelException if the bot is already connected to some other voice channel.
	 */
	public void joinVC(IVoiceChannel voiceChannel) {
		Preconditions.checkNotNull(voiceChannel, "voiceChannel must be non-null.");
		
		IVoiceChannel currentVc = guild.getConnectedVoiceChannel();
		
		if (guild.getConnectedVoiceChannel() == null) {
			voiceChannel.join();
			
			this.audioPlayer = audioPlayerManager.createPlayer();
			guild.getAudioManager().setAudioProvider(new LavaplayerAudioProvider(audioPlayer));
			
		} else if (currentVc.getLongID() != voiceChannel.getLongID()) {
			throw new AlreadyInAnotherVoiceChannelException("Already connected to another voice channel.", currentVc);
		}
	}
	
	/**
	 * Leaves whatever voice channel the bot is in.
	 * No-op if it's not in any voice channel.
	 */
	public void leaveVC() {
		IVoiceChannel currentVc = guild.getConnectedVoiceChannel();
		if (currentVc != null) {
			this.audioPlayer.destroy();
			this.audioPlayer = null;
			currentVc.leave();
		}
	}
	
	/**
	 * Gets an {@link AudioTrack} from a file.
	 * @param audioFile The audio file. Non-null.
	 * @return {@link AudioTrack}.
	 */
	public AudioTrack findTrack(File audioFile) {
		Preconditions.checkNotNull(audioFile, "audioFile must be non-null.");
		
		try {
			SingleTrackLoadResultHandler trackLoadResultHandler = new SingleTrackLoadResultHandler();
			audioPlayerManager.loadItem(audioFile.getAbsolutePath(), trackLoadResultHandler).get();
			AudioTrack audioTrack = trackLoadResultHandler.getAudioTrack();
			
			if (audioTrack == null) {
				throw new TonbotTechnicalFault("Unable to load track at " + audioFile + " for some reason...");
			}
			
			return audioTrack;
			
		} catch (InterruptedException | ExecutionException e) {
			throw new TonbotTechnicalFault("Unable to load track at " + audioFile, e);
		}
	}

	/**
	 * Plays an {@link AudioTrack} in the current voice channel. 
	 * No-op if it's not connected to a voice channel.
	 * @param audioTrack {@link AudioTrack}. Non-null.
	 * @param position The position of the {@link AudioTrack} to play, in milliseconds.
	 */
	public void playInVC(AudioTrack audioTrack, long position) {
		Preconditions.checkNotNull(audioTrack, "audioTrack must be non-null.");
		Preconditions.checkArgument(position >= 0, "position must be positive.");
		
		if (this.audioPlayer != null) {
			audioTrack.setPosition(position);
			audioPlayer.playTrack(audioTrack);
		}
	}
	
	/**
	 * Plays an audio {@link File} immediately. 
	 * @param audioFile An audio {@link File}. Non-null.
	 */
	public void playInVC(File audioFile) {
		Preconditions.checkNotNull(audioFile, "audioFile must be non-null.");
		
		if (this.audioPlayer != null) {
			AudioTrack at = findTrack(audioFile);
			playInVC(at, 0);
		}
	}
	
	/**
	 * Stops playing the current track.
	 */
	public void stopPlaying() {
		if (audioPlayer != null) {
			audioPlayer.stopTrack();
		}
	}
}
