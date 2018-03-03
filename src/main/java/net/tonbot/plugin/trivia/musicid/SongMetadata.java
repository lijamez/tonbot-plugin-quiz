package net.tonbot.plugin.trivia.musicid;

import java.util.Map;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;

@Data
public class SongMetadata {

	private final Map<Tag, String> tags;
	
	@Builder
	private SongMetadata(Map<Tag, String> tags) {
		this.tags = Preconditions.checkNotNull(tags, "tags must be non-null.");
	}
}
