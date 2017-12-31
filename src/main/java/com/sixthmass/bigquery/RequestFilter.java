package com.sixthmass.bigquery;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Random;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import com.google.cloud.bigquery.BigQueryException;
import com.sixthmass.bigquery.model.Event;
import com.sixthmass.bigquery.util.BigQueryUtil;

@WebFilter(filterName = "RequestFilter", urlPatterns="/api/*")
public class RequestFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// nothing to init
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest)request;
		try {
			// catching all requests to api and logging
			Event event = new Event();
			event.name = req.getRequestURI();
			event.time = LocalDateTime.now();
			event.method = req.getMethod();
			event.userId = "user_" + (new Random().nextInt(1000) + 1);
			
			event = augmentEventWithHeaders(event, req);
			
			boolean inserted = BigQueryUtil.eventQueue.offer(event);
			if (!inserted) {
				System.err.println("Event queue is full! inserted: " + inserted + ", queue size: " + BigQueryUtil.eventQueue.size());
			}
		} catch (BigQueryException e) {
			System.err.println("Error inserting event to bigquery: " + e.getMessage() + ", queue size: " + BigQueryUtil.eventQueue.size());
			e.printStackTrace();
		}
		
		chain.doFilter(request, response);
		
	}

	@Override
	public void destroy() {
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
