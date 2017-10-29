package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

import net.tonbot.plugin.trivia.model.Choice
import net.tonbot.plugin.trivia.model.MultipleChoiceQuestionTemplate
import spock.lang.Specification

class MultipleChoiceQuestionTemplateTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
		this.objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
	}

	def "successful deserialization"() {
		given: 
		String questionStr = '''
        {
			"type" : "multiple_choice",
			"points" : 5,
            "question" : "Sample Question?",
			"choices" : [
				{
					"value" : "Choice A",
					"isCorrect" : true
				},
				{
					"value" : "Choice B",
					"isCorrect" : false
				}
			]
        }
		'''
		
		and:
		MultipleChoiceQuestionTemplate expectedQuestion = MultipleChoiceQuestionTemplate.builder()
			.points(5)
			.question("Sample Question?")
			.choices([
				Choice.builder()
					.value("Choice A")
					.isCorrect(true)
					.build(),
				Choice.builder()
					.value("Choice B")
					.isCorrect(false)
					.build()
			])
			.build()
				
		
		when:
		MultipleChoiceQuestionTemplate question = objMapper.readValue(questionStr, MultipleChoiceQuestionTemplate.class)
		
		then:
		question == expectedQuestion
	}
	
	def "invalid question - no correct choices"() {
		given:
		String questionStr = '''
        {
			"type" : "multiple_choice",
			"points" : 5,
            "question" : "Sample Question?",
			"choices" : [
				{
					"value" : "Choice B",
					"isCorrect" : false
				}
			]
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MultipleChoiceQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid question - no incorrect choices"() {
		given:
		String questionStr = '''
        {
			"type" : "multiple_choice",
			"points" : 5,
            "question" : "Sample Question?",
			"choices" : [
				{
					"value" : "Choice A",
					"isCorrect" : true
				}
			]
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MultipleChoiceQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid question - no choices"() {
		given:
		String questionStr = '''
        {
			"type" : "multiple_choice",
			"points" : 5,
            "question" : "Sample Question?",
			"choices" : []
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MultipleChoiceQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid question - empty question"() {
		given:
		String questionStr = '''
        {
			"type" : "multiple_choice",
			"points" : 5,
            "question" : "",
			"choices" : [
				{
					"value" : "Choice A",
					"isCorrect" : true
				},
				{
					"value" : "Choice B",
					"isCorrect" : false
				}
			]
        }
		'''
		
		when:
		objMapper.readValue(questionStr, MultipleChoiceQuestionTemplate.class)
		
		then:
		thrown JsonMappingException
	}
}
