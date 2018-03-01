package net.tonbot.plugin.trivia;

/**
 * A discrete difficulty. Its natural sort order is also the order in which difficulties increase.
 */
enum Difficulty {

	EASY(
			"Easy", 0.75, 4, 0), 
	MEDIUM(
			"Medium", 0.5, 5, 0.3), 
	HARD(
			"Hard", 0.25, 8, 0.6);

	private final String friendlyName;
	private final double scoreDecayFactor;
	private final int maxMultipleChoices;
	private final double minQuestionDifficulty;

	private Difficulty(
			String friendlyName, 
			double scoreDecayFactor, 
			int maxMultipleChoices,
			double minQuestionDifficulty) {
		
		this.friendlyName = friendlyName;
		this.scoreDecayFactor = scoreDecayFactor;
		this.maxMultipleChoices = maxMultipleChoices;
		this.minQuestionDifficulty = minQuestionDifficulty;
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
	
	/**
	 * A question difficulty from a scale of 0 to 1, inclusive, where 0 is the easiest and the 1 is the hardest. 
	 * Any question that have a difficulty greater than or equal to this value is considered to be <i>at least</i> this difficulty.
	 * @return The min question difficulty.
	 */
	public double getMinQuestionDifficulty() {
		return minQuestionDifficulty;
	}
	
	/**
	 * Gets the next highest difficulty.
	 * @return The next highest difficulty. Null if there is no higher difficulty.
	 */
	public Difficulty getNextDifficulty() {
		if (this.ordinal() >= Difficulty.values().length - 1) {
			return null;
		}
		
		return Difficulty.values()[this.ordinal() + 1];
	}
}
