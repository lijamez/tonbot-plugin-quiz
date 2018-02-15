package net.tonbot.plugin.trivia;

enum Difficulty {

	EASY("Easy"), MEDIUM("Medium"), HARD("Hard");

	private final String friendlyName;

	private Difficulty(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	public String getFriendlyName() {
		return friendlyName;
	}
}
