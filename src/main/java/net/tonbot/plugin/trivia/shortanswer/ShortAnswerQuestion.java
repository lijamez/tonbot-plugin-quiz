package net.tonbot.plugin.trivia.shortanswer;

import java.io.File;
import java.util.List;

import com.google.common.base.Preconditions;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.tonbot.plugin.trivia.model.Question;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShortAnswerQuestion extends Question {

	private final String query;
	private final List<String> answers;
	
	@Builder
	private ShortAnswerQuestion(long points, File image, String query, List<String> answers) {
		super(points, image);
		
		this.query = Preconditions.checkNotNull(query, "query must be non-null.");
		
		this.answers = Preconditions.checkNotNull(answers, "answers must be non-null.");
		Preconditions.checkArgument(!answers.isEmpty(), "answers must not be empty.");
	}
}
