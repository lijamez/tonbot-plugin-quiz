package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.ObjectMapper

import net.tonbot.plugin.trivia.model.Choice
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate
import net.tonbot.plugin.trivia.model.MusicIdentificationQuestionTemplate
import net.tonbot.plugin.trivia.model.QuestionTemplateBundle
import net.tonbot.plugin.trivia.model.ShortAnswerQuestionTemplate
import net.tonbot.plugin.trivia.model.TrackProperty
import spock.lang.Specification

class QuestionTemplateBundleTest extends Specification {

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
		QuestionTemplateBundle expectedQuestionBundle = QuestionTemplateBundle.builder()
			.questionTemplates([
				ShortAnswerQuestionTemplate.builder()
					.question("What was the former name of Thomas Bergersen's 2011 album, Illusions?")
					.answers(["Nemesis II", "Nemesis 2"])
					.build(),
				MultipleChoiceQuestionTemplate.builder()
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
				MusicIdentificationQuestionTemplate.builder()
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
		QuestionTemplateBundle qb = objMapper.readValue(questionBundleStr, QuestionTemplateBundle.class)
		
		then:
		qb == expectedQuestionBundle
	}
}