package net.tonbot.plugin.trivia;

import java.io.File;
import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

import net.tonbot.common.Activity;
import net.tonbot.common.TonbotPlugin;
import net.tonbot.common.TonbotPluginArgs;

public class TriviaPlugin extends TonbotPlugin {

	private final Injector injector;

	public TriviaPlugin(TonbotPluginArgs pluginArgs) {
		super(pluginArgs);

		File triviaPacksDir = pluginArgs.getPluginDataDir();
		triviaPacksDir.mkdirs();

		this.injector = Guice.createInjector(new TriviaModule(
				pluginArgs.getDiscordClient(),
				pluginArgs.getBotUtils(),
				pluginArgs.getColor(),
				triviaPacksDir));
	}

	@Override
	public Set<Activity> getActivities() {
		return injector.getInstance(Key.get(new TypeLiteral<Set<Activity>>() {
		}));
	}

	public Set<Object> getRawEventListeners() {
		return injector.getInstance(Key.get(new TypeLiteral<Set<Object>>() {
		}));
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
