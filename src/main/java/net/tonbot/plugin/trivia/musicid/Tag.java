package net.tonbot.plugin.trivia.musicid;

import org.jaudiotagger.tag.FieldKey;

public enum Tag {
	COMPOSER(FieldKey.COMPOSER),
	TITLE(FieldKey.TITLE),
	ALBUM(FieldKey.ALBUM);
	
	private final FieldKey fieldKey;
	
	private Tag(FieldKey fieldKey) {
		this.fieldKey = fieldKey;
	}
	
	public FieldKey getFieldKey() {
		return fieldKey;
	}
}
