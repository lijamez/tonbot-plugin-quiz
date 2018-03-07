package net.tonbot.plugin.trivia.multiplechoice;

import java.io.File;
import java.util.List;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.Choice;
import net.tonbot.plugin.trivia.model.Question;

/**
 * The manifestation of a particular music ID question.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MultipleChoiceQuestion extends Question {

	private final String query;
	private final List<Choice> choices;
	
	@Builder
	private MultipleChoiceQuestion(long points, File image, String query, List<Choice> choices) {
		super(points, image);
		
		this.query = Preconditions.checkNotNull(query, "query must be non-null.");
		
		this.choices = Preconditions.checkNotNull(choices, "choices must be non-null.");
	}
}
