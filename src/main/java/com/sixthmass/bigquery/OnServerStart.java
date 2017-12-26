package com.sixthmass.bigquery;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.sixthmass.bigquery.model.Event;
import com.sixthmass.bigquery.util.BigQueryUtil;

@WebListener
public class OnServerStart implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		
		try {
			if (!BigQueryUtil.tableExists()) {
				Event sampleEvent = new Event();
				BigQueryUtil.createTable(sampleEvent);
			}
			
			BigQueryUtil.startQueueListener();
			
		} catch (Exception e) {
			e.printStackTrace();
			// do nothing. Let the server start anyway
		}
		
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {		
	}

}
