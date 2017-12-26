package com.sixthmass.bigquery.util;

import java.io.IOException;
import java.nio.charset.Charset;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.sixthmass.bigquery.model.Event;
import com.sixthmass.bigquery.model.RestException;

public class JSONUtil {
	
	public static ObjectMapper mapper = new ObjectMapper();
	
	static {
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
	    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	    mapper.setSerializationInclusion(Include.NON_NULL);
	    mapper.registerModule(new ParameterNamesModule())
	          .registerModule(new Jdk8Module())
	          .registerModule(new JavaTimeModule());
	}
	
	public static String eventToJsonString(Event event) throws JsonProcessingException {
		return mapper.writeValueAsString(event);
	}
	
	public static Event jsonToEvent(String json) throws JsonMappingException, JsonParseException, IOException {
		return mapper.readValue(json.getBytes(Charset.forName("utf-8")), Event.class);
	}
	
	public static String toExceptionJson(String message, int code) throws JsonProcessingException {
		return mapper.writeValueAsString(new RestException(code, message));
	}

}
