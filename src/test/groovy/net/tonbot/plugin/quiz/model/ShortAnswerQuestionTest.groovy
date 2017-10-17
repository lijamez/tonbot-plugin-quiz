package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Specification

class ShortAnswerQuestionTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
		this.objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
	}

	def "successful deserialization"() {
		given: 
		String questionStr = '''
        {
			"type":"short_answer",
            "question" : "Test Question?",
			"answers" : ["A", "B", "C"]
        }
		'''
		
		and:
		ShortAnswerQuestion expectedQuestion = ShortAnswerQuestion.builder()
			.question("Test Question?")
			.answers(["A", "B", "C"])
			.build()
		
		when:
		ShortAnswerQuestion question = objMapper.readValue(questionStr, ShortAnswerQuestion.class)
		
		then:
		question == expectedQuestion
	}
	
	def "invalid question - blank question"() {
		given:
		String questionStr = '''
        {
			"type":"short_answer",
            "question" : "",
			"answers" : ["A", "B", "C"]
        }
		'''
		
		when:
		objMapper.readValue(questionStr, ShortAnswerQuestion.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid question - no answers"() {
		given:
		String questionStr = '''
        {
			"type":"short_answer",
            "question" : "Test Question?",
			"answers" : []
        }
		'''
		
		when:
		objMapper.readValue(questionStr, ShortAnswerQuestion.class)
		
		then:
		thrown JsonMappingException
	}
}
