package net.tonbot.plugin.trivia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Builder;
import lombok.Data;

@Data
public class QuestionBundle {

	private final List<Question> questions;

	@Builder
	@JsonCreator
	public QuestionBundle(
			@JsonProperty("questions") List<Question> questions) {
		Preconditions.checkNotNull(questions, "questions must be non-null.");
		this.questions = ImmutableList.copyOf(questions);
	}
}
