package net.tonbot.plugin.quiz;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.tonbot.common.Activity;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.common.TonbotPluginArgs;

public class QuizPlugin extends TonbotPlugin {

	public QuizPlugin(TonbotPluginArgs pluginArgs) {
		super(pluginArgs);
	}

	@Override
	public Set<Activity> getActivities() {
		return ImmutableSet.of();
	}

	@Override
	public String getActionDescription() {
		return "Play Trivia";
	}

	@Override
	public String getFriendlyName() {
		return "Quiz";
	}

}
