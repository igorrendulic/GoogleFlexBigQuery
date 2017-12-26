package com.sixthmass.bigquery.model;

public class RestException {
	public int code;
	public String message;
	
	public RestException() {}
	
	public RestException(int code, String message) {
		this.code = code;
		this.message = message;
	}
}
