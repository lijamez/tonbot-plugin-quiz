package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.ObjectMapper

import net.tonbot.plugin.trivia.model.Choice
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestion
import net.tonbot.plugin.trivia.model.MusicIdentificationQuestion
import net.tonbot.plugin.trivia.model.QuestionBundle
import net.tonbot.plugin.trivia.model.ShortAnswerQuestion
import net.tonbot.plugin.trivia.model.TrackProperty
import spock.lang.Specification

class QuestionBundleTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
	}

	def "complex deserialization test"() {
		given: "a question bundle with all types of questions"
		String questionBundleStr = '''
		{
			"questions" : [
				{
					"type" : "short_answer",
					"question" : "What was the former name of Thomas Bergersen's 2011 album, Illusions?",
					"answers" : ["Nemesis II", "Nemesis 2"]
				},
				{
				    "type" : "multiple_choice",
				    "question": "What was Two Steps From Hell's first public album?",
				    "choices" : [
				        {
				            "value" : "Invincible",
				            "isCorrect" : true
				        },
				        {
				            "value" : "Illusions",
				            "isCorrect" : false
				        },
				        {
				            "value" : "Archangel",
				            "isCorrect" : false
				        }
				    ]
				},
				{
				    "type":"music_identification",
				    "track_path":"mySong.mp3",
				    "ask_for" : ["RELEASE_YEAR", "TITLE", "ALBUM_NAME", "ARTIST_NAME", "COMPOSER"]
				}
			]
		}
		'''
		
		and:
		QuestionBundle expectedQuestionBundle = QuestionBundle.builder()
			.questions([
				ShortAnswerQuestion.builder()
					.question("What was the former name of Thomas Bergersen's 2011 album, Illusions?")
					.answers(["Nemesis II", "Nemesis 2"])
					.build(),
				MultipleChoiceQuestion.builder()
					.question("What was Two Steps From Hell's first public album?")
					.choices([
						Choice.builder()
							.isCorrect(true)
							.value("Invincible")
							.build(),
						Choice.builder()
							.isCorrect(false)
							.value("Illusions")
							.build(),
						Choice.builder()
							.isCorrect(false)
							.value("Archangel")
							.build()
						])
					.build(),
				MusicIdentificationQuestion.builder()
					.trackPath("mySong.mp3")
					.askFor([
						TrackProperty.RELEASE_YEAR,
						TrackProperty.TITLE,
						TrackProperty.ALBUM_NAME,
						TrackProperty.ARTIST_NAME,
						TrackProperty.COMPOSER])
					.build()
				])
			.build()
		
		when:
		QuestionBundle qb = objMapper.readValue(questionBundleStr, QuestionBundle.class)
		
		then:
		qb == expectedQuestionBundle
	}
}
