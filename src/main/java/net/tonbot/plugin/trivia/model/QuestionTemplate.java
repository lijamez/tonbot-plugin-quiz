package net.tonbot.plugin.trivia.model;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = ShortAnswerQuestionTemplate.class, name = "short_answer"),
		@Type(value = MultipleChoiceQuestionTemplate.class, name = "multiple_choice"),
		@Type(value = MusicIdQuestionTemplate.class, name = "music_id") })
public abstract class QuestionTemplate {

	private final long points;
	private final List<String> imagePaths;
	private final Double difficulty;

	public QuestionTemplate(long points, Double difficulty, List<String> imagePaths) {
		Preconditions.checkArgument(points >= 0, "points must not be negative.");
		
		this.points = points;
		
		if (difficulty != null) {
			Preconditions.checkArgument(difficulty >= 0 && difficulty <= 1, "difficulty must be within 0 and 1, inclusive.");
		}
		
		this.difficulty = difficulty;
		
		this.imagePaths = imagePaths == null ? ImmutableList.of() : ImmutableList.copyOf(imagePaths);
	}
	
	public Optional<Double> getDifficulty() {
		return Optional.ofNullable(difficulty);
	}
}
