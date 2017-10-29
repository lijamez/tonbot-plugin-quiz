package net.tonbot.plugin.trivia.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = ShortAnswerQuestionTemplate.class, name = "short_answer"),
		@Type(value = MultipleChoiceQuestionTemplate.class, name = "multiple_choice"),
		@Type(value = MusicIdentificationQuestionTemplate.class, name = "music_identification") })
public abstract class QuestionTemplate {

	private final long points;
	private final List<String> imagePaths;

	public QuestionTemplate(long points, List<String> imagePaths) {
		Preconditions.checkArgument(points >= 0, "points must not be negative.");
		this.points = points;
		this.imagePaths = imagePaths == null ? ImmutableList.of() : ImmutableList.copyOf(imagePaths);
	}
}
