package net.tonbot.plugin.trivia.musicid;

import java.util.Optional;

import org.jaudiotagger.tag.FieldKey;

public enum SongProperty {
	COMPOSER("Composer", FieldKey.COMPOSER),
	TITLE("Title", FieldKey.TITLE),
	ALBUM("Album", FieldKey.ALBUM),
	ARTIST("Artist", FieldKey.ARTIST);
	
	private final String friendlyName;
	private final FieldKey fieldKey;
	
	private SongProperty(String friendlyName, FieldKey fieldKey) {
		this.friendlyName = friendlyName;
		this.fieldKey = fieldKey;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	/**
	 * Gets the {@link FieldKey} associated with {@link SongProperty}, if it exists.
	 * @return A {@link FieldKey}, if applicable.
	 */
	public Optional<FieldKey> getFieldKey() {
		return Optional.ofNullable(fieldKey);
	}
}
