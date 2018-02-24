package net.tonbot.plugin.trivia;

enum Difficulty {

	EASY("Easy", 0.75, 4), 
	MEDIUM("Medium", 0.5, 5), 
	HARD("Hard", 0.25, 8);

	private final String friendlyName;
	private final double scoreDecayFactor;
	private final int maxMultipleChoices;

	private Difficulty(String friendlyName, double scoreDecayFactor, int maxMultipleChoices) {
		this.friendlyName = friendlyName;
		this.scoreDecayFactor = scoreDecayFactor;
		this.maxMultipleChoices = maxMultipleChoices;
	}

	public String getFriendlyName() {
		return friendlyName;
	}
	
	public double getScoreDecayFactor() {
		return scoreDecayFactor;
	}
	
	public int getMaxMultipleChoices() {
		return maxMultipleChoices;
	}
}
