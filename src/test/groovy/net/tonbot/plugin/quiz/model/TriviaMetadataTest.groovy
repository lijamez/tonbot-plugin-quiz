package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Specification

class TriviaMetadataTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
		this.objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
	}

	def "successful deserialization"() {
		given: 
		String serialized = '''
        {
			"name" : "Test Trivia Pack",
            "version" : "0.1",
			"description" : "For Unit Testing"
        }
		'''
		
		and:
		TriviaMetadata expectedMetadata = TriviaMetadata.builder()
			.name("Test Trivia Pack")
			.version("0.1")
			.description("For Unit Testing")
			.build()
		
		when:
		TriviaMetadata metadata = objMapper.readValue(serialized, TriviaMetadata.class)
		
		then:
		metadata == expectedMetadata
	}
	
	def "invalid metadata - blank name"() {
		given: 
		String serialized = '''
        {
			"name" : "",
            "version" : "0.1",
			"description" : "For Unit Testing"
        }
		'''
		
		when:
		TriviaMetadata metadata = objMapper.readValue(serialized, TriviaMetadata.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid metadata - blank version"() {
		given: 
		String serialized = '''
        {
			"name" : "Test Trivia Pack",
            "version" : "",
			"description" : "For Unit Testing"
        }
		'''
		
		when:
		TriviaMetadata metadata = objMapper.readValue(serialized, TriviaMetadata.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid metadata - blank description"() {
		given:
		String serialized = '''
        {
			"name" : "Test Trivia Pack",
            "version" : "0.1",
			"description" : ""
        }
		'''
		
		when:
		TriviaMetadata metadata = objMapper.readValue(serialized, TriviaMetadata.class)
		
		then:
		thrown JsonMappingException
	}
}
