package net.tonbot.plugin.trivia.musicid;

import org.jaudiotagger.tag.FieldKey;

public enum Tag {
	COMPOSER("Composer", FieldKey.COMPOSER),
	TITLE("Title", FieldKey.TITLE),
	ALBUM("Album", FieldKey.ALBUM),
	ARTIST("Artist", FieldKey.ARTIST);
	
	private final String friendlyName;
	private final FieldKey fieldKey;
	
	private Tag(String friendlyName, FieldKey fieldKey) {
		this.friendlyName = friendlyName;
		this.fieldKey = fieldKey;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public FieldKey getFieldKey() {
		return fieldKey;
	}
}
