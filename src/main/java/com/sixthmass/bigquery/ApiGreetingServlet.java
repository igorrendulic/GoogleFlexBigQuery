package com.sixthmass.bigquery;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.sixthmass.bigquery.model.Greeting;
import com.sixthmass.bigquery.util.JSONUtil;

/**
 * Greeting servlet
 */
@WebServlet(name = "GreetingServlet", value = "/api/greeting")
public class ApiGreetingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static AtomicLong counter = new AtomicLong(1);
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("application/json; charset=utf-8");
		
		Greeting greet = new Greeting();
		greet.id = "greeting_" + counter.incrementAndGet();
		greet.userName = "Hello, anonymous";
		
		ServletOutputStream outstream = response.getOutputStream();
		outstream.write(JSONUtil.objectToJsonString(greet).getBytes("utf-8"));
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("application/json; charset=utf-8");
		
		try {
			//parsing input to greeting object
			String requestJson = JSONUtil.readPostJson(request);
			Greeting greeting = JSONUtil.jsonToObject(requestJson, Greeting.class);
			
			//say hello
			greeting.id = "greeting_" + counter.getAndIncrement();
			greeting.userName = "Hello, " + greeting.userName;
			
			// greeting person
			ServletOutputStream outstream = response.getOutputStream();
			outstream.write(JSONUtil.objectToJsonString(greeting).getBytes("utf-8"));
			
		} catch (Exception e) {
			e.printStackTrace();
			JSONUtil.sendError(HttpStatus.SC_BAD_REQUEST, "Bad request", response);
		}
	}
}
