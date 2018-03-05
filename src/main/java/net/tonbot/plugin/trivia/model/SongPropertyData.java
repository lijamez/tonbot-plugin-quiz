package net.tonbot.plugin.trivia.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

import lombok.Data;

@Data
public class SongPropertyData {

	private final List<String> answers;
	
	@JsonCreator()
	public SongPropertyData(@JsonProperty("answers") List<String> answers) {
		
		if (answers != null) {
			Preconditions.checkNotNull(!answers.isEmpty(), "answers, if present, must be non-empty.");
		}
		
		this.answers = answers;
	}
	
	public Optional<List<String>> getAnswers() {
		return Optional.ofNullable(answers);
	}
	
}
