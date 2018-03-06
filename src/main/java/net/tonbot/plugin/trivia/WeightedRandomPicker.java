package net.tonbot.plugin.trivia;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class WeightedRandomPicker {
	
	private final Random random;
	
	@Inject
	public WeightedRandomPicker(Random random) {
		this.random = Preconditions.checkNotNull(random, "random must be non-null.");
	}

	/**
	 * Picks a random key from a map where the values are the weights. Weights may be null, in which case they 
	 * effectively take on the average weight. If all keys are null, then every key has an equal chance of being selected.
	 * @param weightedKeys Map of keys to their weights. Each weight must be a non-negative number. Non-null.
	 * @return A random key, based on the weights.
	 */
	public <T> T pick(Map<T, Long> weightedKeys) {
		Preconditions.checkNotNull(weightedKeys, "weightedKeys must be non-null.");
		Preconditions.checkArgument(!weightedKeys.isEmpty(), "weightedKeys must be non-empty.");
		
		List<T> keys = new ArrayList<>();
		List<Long> weights = new ArrayList<>();
		
		weightedKeys.entrySet().stream()
			.forEach(entry -> {
				keys.add(entry.getKey());
				weights.add(entry.getValue());
			});
		
		return pick(keys, weights);
	}
	
	/**
	 * Picks a random object in the {@code objs} list, taking into account the weights in {@code weights}.
	 * @param objs The objects to pick from. Non-null, non-empty.
	 * @param weights The weights. Each weight must be non-negative. Must be the same size as {@code objs}. 
	 * 			Null elements are permitted; those will take on weights that are the average of the other weights. 
	 * 			If all elements are null, then they effectively have weight 1 each. Non-null, non-empty.
	 * @return One of the items in {@code objs}.
	 */
	public <T> T pick(List<T> objs, List<Long> weights) {
		Preconditions.checkNotNull(objs, "objs must be non-null.");
		Preconditions.checkNotNull(weights, "weights must be non-null.");
		
		Preconditions.checkArgument(!objs.isEmpty(), "objs must not be empty.");
		Preconditions.checkArgument(!weights.isEmpty(), "weights must not be empty.");
		
		Preconditions.checkArgument(objs.size() == weights.size(), "The length of objs and weights must be equal.");
		weights.forEach(w -> {
			if (w != null) {
				Preconditions.checkArgument(w >= 0, "Each weight must be non-negative.");
			}
		});
		
		List<Long> computableWeights = calculateWeightsForComputation(weights);
		
		long bound = computableWeights.stream()
			.mapToLong(w -> w)
			.sum();
		
		int randomNumber = random.nextInt((int) bound) + 1;
		
		long x = 0;
		for (int i = 0; i < computableWeights.size() ; i++) {
			x += computableWeights.get(i);
			if (x >= randomNumber) {
				return objs.get(i);
			}
		}
		
		throw new IllegalStateException("Unable to pick random element. This should never happen.");
		
	}
	
	/**
	 * Fills in null values with the average of the non-null weights.
	 * @param weights The weights, possibly including nulls.
	 * @return Another weights list, with all nulls filled in with the average of the given weights.
	 */
	private List<Long> calculateWeightsForComputation(List<Long> weights) {
		long average = (long) weights.stream()
			.filter(w -> w != null)
			.mapToLong(w -> w)
			.average()
			.orElseGet(() -> 1d);
		
		List<Long> weightsForComputation = new ArrayList<>(weights);
		
		return weightsForComputation.stream()
			.map(w -> w == null ? average : w)
			.collect(Collectors.toList());
	}
}
