package net.tonbot.plugin.trivia;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;

public class FuzzyMatcher {

	public static boolean matches(String messageStr, Iterable<String> candidates) {
		Preconditions.checkNotNull(candidates, "candidate must be non-null.");
		if (messageStr == null) {
			return false;
		}
		
		String normalizedInput = normalize(messageStr);

		for (String candidate : candidates) {
			String normalizedCandidate = normalize(candidate);

			if (StringUtils.equalsIgnoreCase(normalizedInput, normalizedCandidate)) {
				return true;
			}
		}

		return false;
	}

	private static String normalize(String phrase) {
		String normalized = phrase.trim();

		// Multi-space characters should be replaced with a single space
		normalized = normalized.replaceAll("\\s+", " ");

		// Punctuation should be removed.
		normalized = normalized.replaceAll("\\p{Punct}", "");

		return normalized;
	}
}
