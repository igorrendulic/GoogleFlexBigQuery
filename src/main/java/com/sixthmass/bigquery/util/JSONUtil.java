package com.sixthmass.bigquery.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
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
	
	public static String objectToJsonString(Object object) throws JsonProcessingException {
		return mapper.writeValueAsString(object);
	}
	
	public static <T> T jsonToObject(String json, Class<T> clazz) throws JsonMappingException, JsonParseException, IOException {
		return mapper.readValue(json.getBytes(Charset.forName("utf-8")), clazz);
	}
	
	public static String toExceptionJson(String message, int code) throws JsonProcessingException {
		return mapper.writeValueAsString(new RestException(code, message));
	}
	
	/**
	 * Reading input stream from request
	 * @param request
	 * @return
	 * @throws IOException
	 */
	public static String readPostJson(HttpServletRequest request) throws IOException {
		String requestJson = null;
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
			requestJson = buffer.lines().collect(Collectors.joining("\n"));
		}
		return requestJson;
	}
	
	/** 
	 * Error response handling
	 * @param e
	 * @param response
	 * @throws IOException
	 */
	public static void sendError(int code, String message, HttpServletResponse response) throws IOException {
		response.setStatus(code);
		ServletOutputStream outStream = response.getOutputStream();
		outStream.write(JSONUtil.toExceptionJson(message, code).getBytes("utf-8"));
	}

}
