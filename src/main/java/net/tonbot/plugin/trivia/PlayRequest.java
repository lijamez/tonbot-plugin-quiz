package net.tonbot.plugin.trivia;

import javax.annotation.Nonnull;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import net.tonbot.common.Param;

@EqualsAndHashCode
@ToString
public class PlayRequest {

	@Getter
	@Param(name = "trivia pack", ordinal = 0, description = "The trivia pack name.")
	@Nonnull
	private String triviaPackName;

	@Getter
	@Param(name = "difficulty", ordinal = 1, description = "The difficulty.")
	private Difficulty difficulty;
}
