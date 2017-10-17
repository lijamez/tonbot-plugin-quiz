package net.tonbot.plugin.quiz.model

import com.fasterxml.jackson.databind.ObjectMapper

import spock.lang.Specification

class QuizMetadataTest extends Specification {

	ObjectMapper objMapper;

	def setup() {
		this.objMapper = new ObjectMapper();
		this.objMapper.enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
	}

	def "successful deserialization"() {
		given: 
		String serialized = '''
        {
			"name" : "Test Quiz Pack",
            "version" : "0.1",
			"description" : "For Unit Testing"
        }
		'''
		
		and:
		QuizMetadata expectedMetadata = QuizMetadata.builder()
			.name("Test Quiz Pack")
			.version("0.1")
			.description("For Unit Testing")
			.build()
		
		when:
		QuizMetadata metadata = objMapper.readValue(serialized, QuizMetadata.class)
		
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
		QuizMetadata metadata = objMapper.readValue(serialized, QuizMetadata.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid metadata - blank version"() {
		given: 
		String serialized = '''
        {
			"name" : "Test Quiz Pack",
            "version" : "",
			"description" : "For Unit Testing"
        }
		'''
		
		when:
		QuizMetadata metadata = objMapper.readValue(serialized, QuizMetadata.class)
		
		then:
		thrown JsonMappingException
	}
	
	def "invalid metadata - blank description"() {
		given:
		String serialized = '''
        {
			"name" : "Test Quiz Pack",
            "version" : "0.1",
			"description" : ""
        }
		'''
		
		when:
		QuizMetadata metadata = objMapper.readValue(serialized, QuizMetadata.class)
		
		then:
		thrown JsonMappingException
	}
}
