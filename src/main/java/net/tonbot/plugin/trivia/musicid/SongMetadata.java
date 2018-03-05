package net.tonbot.plugin.trivia.musicid;

import java.util.Map;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;

@Data
public class SongMetadata {

	private final Map<SongProperty, String> properties;
	
	@Builder
	private SongMetadata(Map<SongProperty, String> properties) {
		this.properties = Preconditions.checkNotNull(properties, "properties must be non-null.");
	}
}
