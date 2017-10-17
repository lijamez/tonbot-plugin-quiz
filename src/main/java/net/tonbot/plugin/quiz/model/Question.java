package net.tonbot.plugin.quiz.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
		@Type(value = ShortAnswerQuestion.class, name = "short_answer"),
		@Type(value = MultipleChoiceQuestion.class, name = "multiple_choice"),
		@Type(value = MusicIdentificationQuestion.class, name = "music_identification") })
public abstract class Question {

}
