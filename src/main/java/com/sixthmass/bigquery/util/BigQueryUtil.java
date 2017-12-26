package com.sixthmass.bigquery.util;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryError;
import com.google.cloud.bigquery.BigQueryOptions;
import com.google.cloud.bigquery.Dataset;
import com.google.cloud.bigquery.DatasetInfo;
import com.google.cloud.bigquery.InsertAllRequest;
import com.google.cloud.bigquery.InsertAllRequest.Builder;
import com.google.cloud.bigquery.InsertAllResponse;
import com.google.cloud.bigquery.LegacySQLTypeName;
import com.google.cloud.bigquery.Schema;
import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.cloud.bigquery.TableId;
import com.google.cloud.bigquery.TableInfo;
import com.google.gson.Gson;
import com.sixthmass.bigquery.model.Event;

public class BigQueryUtil {

	private static BigQuery bigQuery = BigQueryOptions.getDefaultInstance().getService();

	private static final String BIGQUERY_DATASET = "stream";
	private static final String BIGQUERY_TABLE = "events";
	private static Gson gson = new Gson();
	private static ExecutorService queueService = Executors.newFixedThreadPool(1);
	
	public static final BlockingQueue<Event> eventQueue = new LinkedBlockingQueue<>();
	
	public static void startQueueListener() {
		Runnable startTask = () -> {
			try {
				
				while (true) {
					int numberOfIngested = 0;
					if (eventQueue.size() > 0) {
						List<Event> events = new ArrayList<>();
						eventQueue.drainTo(events, 50);
						
						System.out.println("About to ingest: " + events.size() + " events");
						insertEventsToBigQuery(events);
						
						numberOfIngested = eventQueue.size();
					}
					if (numberOfIngested < 50) { // delay if not a lot of events coming in
						Thread.sleep(5000);
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		queueService.submit(startTask);
	}
	

	/**
	 * Check if BigQuery Table exists
	 */
	public static boolean tableExists() {
		TableId tableId = TableId.of(BIGQUERY_DATASET, BIGQUERY_TABLE);
		Table existsTable = bigQuery.getTable(tableId);
		if (existsTable == null) {
			return false;
		}
		return existsTable.exists();
	}

	/**
	 * Parses the given JSON string and returns the extracted schema.
	 *
	 * @param fields
	 *            a string to read the TableSchema from.
	 * @return the List of TableFieldSchema described by the string fields.
	 */
	private static List<com.google.cloud.bigquery.Field> getEventSchema() {

		List<com.google.cloud.bigquery.Field> fieldsList = new ArrayList<>();

		Field[] fields = Event.class.getFields();

		for (Field field : fields) {
			Class<?> type = field.getType();
			String name = field.getName();
			LegacySQLTypeName schematype = LegacySQLTypeName.STRING;

			switch (type.getName()) {
			case "java.lang.String":
				schematype = LegacySQLTypeName.STRING;
				break;
			case "java.lang.Integer":
				schematype = LegacySQLTypeName.INTEGER;
				break;
			case "java.lang.Long":
				schematype = LegacySQLTypeName.INTEGER;
				break;
			case "java.util.Date":
				schematype = LegacySQLTypeName.DATETIME;
				break;
			case "java.util.Boolean":
				schematype = LegacySQLTypeName.BOOLEAN;
				break;
			case "java.lang.Double":
				schematype = LegacySQLTypeName.FLOAT;

			}
			com.google.cloud.bigquery.Field fieldDef = com.google.cloud.bigquery.Field.of(name, schematype);

			fieldsList.add(fieldDef);
		}
		return fieldsList;
	}

	/**
	 * Create bigquery table
	 * 
	 * @param event
	 * @param tableId
	 * @throws JsonProcessingException
	 */
	public static void createTable(Event event) throws Exception {

		Dataset dataset = bigQuery.getDataset(BIGQUERY_DATASET);
		if (dataset == null) {
			bigQuery.create(DatasetInfo.newBuilder(BIGQUERY_DATASET).build());
		}

		List<com.google.cloud.bigquery.Field> tableFieldSchema = getEventSchema();
		Schema schema = Schema.of(tableFieldSchema);
		TableId table = TableId.of(BIGQUERY_DATASET, BIGQUERY_TABLE);

		
		StandardTableDefinition tableDefinition = StandardTableDefinition.of(schema);
		com.google.cloud.bigquery.Table createdTable = bigQuery.create(TableInfo.of(table, tableDefinition));
		System.out.println("Created table: at" + createdTable.getCreationTime());

	}

	/**
	 * Creating
	 * 
	 * @param row
	 * @param userKey
	 */
	public static void insertEventsToBigQuery(List<Event> eventList) {

		try {

			if (eventList != null && eventList.size() > 0) {

				YearMonth thisMonth = YearMonth.now();
				String suffix = thisMonth.getYear() + "_" + thisMonth.getMonth().name();
				
				List<Map<String, Object>> rows = new ArrayList<>();
				
				eventList.stream().forEach(event -> {
					try {
						Map<String, Object> rowData = gson.<Map<String, Object>>fromJson(JSONUtil.eventToJsonString(event), (new HashMap<String, Object>()).getClass());
						rows.add(rowData);
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				});
			
				streamRows(bigQuery, BIGQUERY_TABLE, suffix, rows);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void streamRows(final BigQuery bigquery, final String tableId, final String suffix,
			final List<Map<String, Object>> rows) throws IOException {

		TableId table = TableId.of(BIGQUERY_DATASET, tableId);
		Builder insertRequestBuilder = InsertAllRequest.newBuilder(table);
		insertRequestBuilder.setTemplateSuffix(suffix);

		for (Map<String, Object> row : rows) {
			insertRequestBuilder.addRow(row);
		}

		InsertAllRequest insertallRows = insertRequestBuilder.build();

		InsertAllResponse insertResponse = bigquery.insertAll(insertallRows);
		if (insertResponse.hasErrors()) {
			// concatenate all errors and print them out 
			Map<Long, List<BigQueryError>> errors = insertResponse.getInsertErrors();
			if (errors != null) {
				errors.values().stream().forEach(list -> {
					if (list != null) {
						String errorMessageConcat = list.stream().map(err -> err.getMessage())
								.collect(Collectors.joining("; "));
						System.out.println(errorMessageConcat);
					}
				});
			}
		}
	}

}
