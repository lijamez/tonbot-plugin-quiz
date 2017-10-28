package net.tonbot.plugin.trivia.model;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Preconditions;

import lombok.Data;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = ShortAnswerQuestion.class, name = "short_answer"),
		@Type(value = MultipleChoiceQuestion.class, name = "multiple_choice"),
		@Type(value = MusicIdentificationQuestion.class, name = "music_identification") })
public abstract class Question {

	private final long points;
	private final String imageUrl;

	public Question(long points, String imageUrl) {
		Preconditions.checkArgument(points >= 0, "points must not be negative.");
		this.points = points;
		this.imageUrl = imageUrl;
	}

	public Optional<String> getImageUrl() {
		return Optional.ofNullable(imageUrl);
	}
}
