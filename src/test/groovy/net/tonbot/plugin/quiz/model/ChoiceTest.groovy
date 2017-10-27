package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

import net.tonbot.plugin.trivia.model.Choice
import spock.lang.Specification

class ChoiceTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
		this.objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
	}

	def "successful deserialization"() {
		given: 
		String choiceStr = '''
        {
            "value" : "Invincible",
            "isCorrect" : true
        }
		'''
		
		and:
		Choice expectedChoice = Choice.builder()
			.value("Invincible")
			.isCorrect(true)
			.build()
		
		when:
		Choice choice = objMapper.readValue(choiceStr, Choice.class)
		
		then:
		choice == expectedChoice
	}
	
	def "invalid format"(String choiceStr) {

		when:
		Choice choice = objMapper.readValue(choiceStr, Choice.class)
		
		then:
		thrown JsonMappingException
		
		where:
		choiceStr                                   | _
		'''{"value" : "", "isCorrect" : true}'''    | _
		'''{"value" : null, "isCorrect" : true}'''  | _
		'''{"value" : "foo", "isCorrect" : null}''' | _
		'''{"value" : "foo"}'''                     | _
		'''{"isCorrect" : true}'''                  | _
		'''{"isCorrect" : "foo"}'''                 | _
		'''{}'''                                    | _
	}
}
