package net.tonbot.plugin.trivia.model;

import java.io.File;
import java.util.Optional;

import com.google.common.base.Preconditions;

import lombok.Data;

@Data
public abstract class Question {

	private final long points;
	
	private final File image;
	
	protected Question(long points, File image) {
		this.points = points;
		this.image = image;
		
		if (image != null) {
			Preconditions.checkArgument(image.exists(), "image must exist.");
		}
	}
	
	public Optional<File> getImage() {
		return Optional.ofNullable(image);
	}
}
