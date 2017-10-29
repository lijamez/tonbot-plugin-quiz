package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

import net.tonbot.plugin.trivia.model.MusicIdentificationQuestionTemplate
import net.tonbot.plugin.trivia.model.TrackProperty
import spock.lang.Specification

class MusicIdentificationQuestionTemplateTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
		this.objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
	}

	def "successful deserialization"() {
		given: 
		String questionStr = '''
        {
			"type" : "music_identification",
			"points" : 5,
            "track_path" : "test.mp3",
			"ask_for" : ["ARTIST_NAME", "TITLE"]
        }
		'''
		
		and:
		MusicIdentificationQuestionTemplate expectedQuestion = MusicIdentificationQuestionTemplate.builder()
			.points(5)
			.trackPath("test.mp3")
			.askFor([
				TrackProperty.ARTIST_NAME,
				TrackProperty.TITLE
			])
			.build()
		
		when:
		MusicIdentificationQuestionTemplate question = objMapper.readValue(questionStr, MusicIdentificationQuestionTemplate.class)
		
		then:
		question == expectedQuestion
	}
	
	def "invalid question - blank track path"() {
		given:
		String questionStr = '''
        {
			"type" : "music_identification",
			"points" : 5,
            "track_path" : "",
			"ask_for" : ["ARTIST_NAME", "TITLE"]
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MusicIdentificationQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid question - nothing to ask for"() {
		given:
		String questionStr = '''
        {
			"type" : "music_identification",
			"points" : 5,
            "track_path" : "",
			"ask_for" : []
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MusicIdentificationQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
}
