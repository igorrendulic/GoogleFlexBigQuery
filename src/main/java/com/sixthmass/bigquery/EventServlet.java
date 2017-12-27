package com.sixthmass.bigquery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.google.cloud.bigquery.BigQueryException;
import com.sixthmass.bigquery.model.Event;
import com.sixthmass.bigquery.util.BigQueryUtil;
import com.sixthmass.bigquery.util.JSONUtil;

/**
 * Simple event servlet
 */
@WebServlet(name = "EventServlet", value = "/api/event")
public class EventServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//simple ping 
		response.setContentType("application/json; charset=utf-8");
		ServletOutputStream outstream = response.getOutputStream();
		
		Event event = new Event();
		event.id = "example event id 1";
		event.userId = "user1";
		event.time = LocalDateTime.now();
		outstream.write(JSONUtil.eventToJsonString(event).getBytes("utf-8"));
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("application/json");
		
		try (BufferedReader buffer = new BufferedReader(new InputStreamReader(request.getInputStream()))) {
			String requestJson = buffer.lines().collect(Collectors.joining("\n"));
			
			//parsing input to event
			Event event = JSONUtil.jsonToEvent(requestJson);
			
			// adding header information (city, region, country)
			event = augmentEventWithHeaders(event, request);
			
			// adding event to big query queue
			boolean inserted = BigQueryUtil.eventQueue.offer(event);
			if (!inserted) {
				System.err.println("Event queue is full! inserted: " + inserted + ", queue size: " + BigQueryUtil.eventQueue.size());
				throw new BigQueryException(HttpStatus.SC_BAD_REQUEST, "Queue is full");
			}
			// ping response of the event
			ServletOutputStream outstream = response.getOutputStream();
			outstream.write(JSONUtil.eventToJsonString(event).getBytes("utf-8"));
			
		} catch (BigQueryException e) {
			e.printStackTrace();
			sendError(HttpStatus.SC_EXPECTATION_FAILED, "Queue is full", response);
		} catch (Exception e) {
			e.printStackTrace();
			sendError(HttpStatus.SC_BAD_REQUEST, "Bad request", response);
		}
		
	}
	
	/** 
	 * Error response handling
	 * s
	 * @param e
	 * @param response
	 * @throws IOException
	 */
	private void sendError(int code, String message, HttpServletResponse response) throws IOException {
		response.setStatus(code);
		ServletOutputStream outStream = response.getOutputStream();
		outStream.write(JSONUtil.toExceptionJson(message, code).getBytes("utf-8"));
	}
	
	private Event augmentEventWithHeaders(Event event, HttpServletRequest request) {
		if (event.time == null) {
			event.time = LocalDateTime.now();
		}
		String country = request.getHeader("X-AppEngine-Country");
		String city = request.getHeader("X-AppEngine-City");
		String region = request.getHeader("X-AppEngine-Region");
		event.country = country;
		event.city = city;
		event.region = region;
		return event;
	}

}
