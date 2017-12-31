package com.sixthmass.bigquery;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.sixthmass.bigquery.model.Ping;
import com.sixthmass.bigquery.util.JSONUtil;

/**
 * Simple ping Servlet
 */
@WebServlet(name = "PingServlet", value = "/api/ping")
public class ApiPingServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		//simple ping 
		response.setContentType("application/json; charset=utf-8");
		
		Ping ping = new Ping();
		ping.time = LocalDateTime.now();
		ping.serverName = request.getServerName();
		
		ServletOutputStream outstream = response.getOutputStream();
		outstream.write(JSONUtil.objectToJsonString(ping).getBytes("utf-8"));
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("application/json; charset=utf-8");

		try {
			//parsing input to ping object
			String requestJson = JSONUtil.readPostJson(request);
			
			Ping ping = JSONUtil.jsonToObject(requestJson, Ping.class);
			ping.serverName = request.getServerName();
			ping.time = LocalDateTime.now();
			
			// ping response
			ServletOutputStream outstream = response.getOutputStream();
			outstream.write(JSONUtil.objectToJsonString(ping).getBytes("utf-8"));
			
		} catch (Exception e) {
			e.printStackTrace();
			JSONUtil.sendError(HttpStatus.SC_BAD_REQUEST, "Bad request", response);
		}
		
	}

}
