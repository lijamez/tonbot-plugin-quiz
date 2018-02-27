package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

import net.tonbot.plugin.trivia.model.MusicIdQuestionTemplate
import net.tonbot.plugin.trivia.musicid.Tag
import spock.lang.Specification

class MusicIdQuestionTemplateTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
		this.objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
	}

	def "successful deserialization"() {
		given: 
		String questionStr = '''
        {
			"type" : "music_id",
			"points" : 5,
            "audio" : "test.mp3",
			"tags" : ["TITLE", "ALBUM"]
        }
		'''
		
		and:
		MusicIdQuestionTemplate expectedQuestion = MusicIdQuestionTemplate.builder()
			.points(5)
			.audioPath("test.mp3")
			.tags([
				Tag.TITLE,
				Tag.ALBUM
			] as Set)
			.build()
		
		when:
		MusicIdQuestionTemplate question = objMapper.readValue(questionStr, MusicIdQuestionTemplate.class)
		
		then:
		question == expectedQuestion
	}
	
	def "invalid question - blank track path"() {
		given:
		String questionStr = '''
        {
			"type" : "music_id",
			"points" : 5,
            "audio" : "",
			"tags" : ["TITLE", "ALBUM"]
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MusicIdQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid question - nothing to ask for"() {
		given:
		String questionStr = '''
        {
			"type" : "music_id",
			"points" : 5,
            "audio" : "test.mp3",
			"tags" : []
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MusicIdQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
}
