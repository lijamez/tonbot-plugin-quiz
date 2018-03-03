package net.tonbot.plugin.trivia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class FuzzyMatcher {
	
	private final Map<String, List<String>> normalizedSynonymsIndex;
	
	public FuzzyMatcher(List<List<String>> synonyms) {
		if (synonyms == null) {
			this.normalizedSynonymsIndex = ImmutableMap.of();
		} else {
			this.normalizedSynonymsIndex = generateNormalizedSynonymsIndex(synonyms);
		}
	}
	
	private Map<String, List<String>> generateNormalizedSynonymsIndex(List<List<String>> synonyms) {
		List<List<String>> normalizedSynonyms = synonyms.stream()
			.map(group -> 
				group.stream()
					.map(this::normalize)
					.collect(Collectors.toList())
			)
			.collect(Collectors.toList());
		
		
		Map<String, List<String>> normalizedSynonymIndex = new HashMap<>();
		
		for (List<String> group : normalizedSynonyms) {
			for (String term : group) {
				normalizedSynonymIndex.put(term, group);
			}
		}
		
		return normalizedSynonymIndex;
	}

	public boolean matches(String messageStr, Iterable<String> candidates) {
		Preconditions.checkNotNull(candidates, "candidate must be non-null.");
		
		if (messageStr == null) {
			return false;
		}
		
		String normalizedInput = normalize(messageStr);
		List<String> normalizedSynonyms = normalizedSynonymsIndex.getOrDefault(normalizedInput, ImmutableList.of());
		
		Set<String> inputAndSynonyms = ImmutableSet.<String>builder()
				.add(normalizedInput)
				.addAll(normalizedSynonyms)
				.build();
		
		for (String normalizedInputOrSynonym : inputAndSynonyms) {
			for (String candidate : candidates) {
				String normalizedCandidate = normalize(candidate);

				if (StringUtils.equalsIgnoreCase(normalizedInputOrSynonym, normalizedCandidate)) {
					return true;
				}
			}
		}

		return false;
	}

	private String normalize(String phrase) {
		String normalized = phrase.trim();

		// Punctuation should be removed.
		normalized = normalized.replaceAll("\\p{Punct}", "");
		
		// Multi-space characters should be replaced with a single space
		normalized = normalized.replaceAll("\\s+", " ");
		
		normalized = normalized.toLowerCase();

		return normalized;
	}
}
