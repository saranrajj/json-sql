package json.sql.commands.processor;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jline.utils.Log;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.wnameless.json.flattener.FlattenMode;
import com.github.wnameless.json.flattener.JsonFlattener;

import json.sql.commands.configstore.InMemoryConfigStore;
import json.sql.commands.constants.ConfigConstants;
import json.sql.commands.constants.Types;
import json.sql.commands.exception.JsonSqlException;
import json.sql.commands.exception.ProcessingException;
import json.sql.commands.model.Condition;
import json.sql.commands.model.Query;
import json.sql.commands.model.RequestObject;
import json.sql.commands.model.ResponseObject;
import json.sql.commands.model.ResultSet;
import json.sql.commands.model.Row;
import json.sql.commands.model.SourceConfig;
import json.sql.commands.model.wrapper.ConditionalLiteralWrapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryProcessor {
	
	static ObjectMapper mapper = new ObjectMapper();
	
	static RestTemplate restTemplate = new RestTemplate();
	
	static Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");	
		
	
	public static void executeQuery(Query query) throws JsonSqlException{
		RequestObject requestObject = RequestObject.builder().query(query)
				.pathParams(new HashMap<>())
				.build();
		processPathParam(requestObject);
		parseWhereClause(requestObject);
		
		ResponseObject responseObject = fetchDataFromApiSource(requestObject);
		if(null!=responseObject.getHeaders()) {
			System.out.println("Response headers:");
			responseObject.getHeaders().forEach((key,value)->{
				System.out.println(key+": "+value);	
			});
			
		}
		if(!responseObject.isError()) {
			if(requestObject.getQuery().getColumns().equals("*")) {
				System.out.println("Response code:"+responseObject.getResponseCode());
				System.out.println(responseObject.getResponseText());					
			}
			else {
				try {
					List<Map<String,Object>> result = processResponseData(requestObject.getQuery(),responseObject.getResponseText());
					LinkedList columns = addQueryColumnHeaders(requestObject.getQuery());
					ResultSet resultSet = processQueryResult(requestObject,result,columns);
					processConditions(resultSet, requestObject);
					processResultSetForProjection(resultSet, requestObject);					
					print(resultSet);
					if(!query.getExport().isEmpty()) {
						writeToCSV(resultSet,requestObject);
					}
				}
				catch(Exception ex) {
					System.out.println("Error while processing the request!!!");
					ex.printStackTrace();
				}
			}
		
		}		
	}
	
					
	
	public static ResponseObject fetchDataFromApiSource(RequestObject requestObject) throws JsonSqlException {		
		ResponseObject responseObject = ResponseObject.builder()
				.isError(true)
				.build();
				
		String responseText = "";
		Optional<SourceConfig> sourceConfigOptional = InMemoryConfigStore.fetchConfig(requestObject.getQuery().getSource(),false);
		if(sourceConfigOptional.isPresent()) {
			String requestUrl = requestObject.getProcessedUrl();
			try {
				requestObject.parseHeadParams();
				HttpHeaders headers = new HttpHeaders();
				if(null!=requestObject.getHeaders()) {					
					requestObject.getHeaders().forEach((key,value)->{
						headers.add(key, value);
					});					
				}
				if(null!=requestObject.getCookies()) {
					requestObject.getCookies().forEach((key,value)->{
						headers.add("Cookie", key+"="+value+";");
				});
				}				
				HttpEntity<String> request = new HttpEntity<String>(null, headers);
				ResponseEntity<String> response =restTemplate.exchange(requestUrl, HttpMethod.GET, request, String.class);
				
				response.getHeaders().forEach((key,value)->{
					if(null!=value) {
						responseObject.addHeaders(key, value.toString());
					}
				});
				responseObject.setResponseCode(response.getStatusCodeValue());
				responseObject.setResponseText((null==response.getBody())?"null":response.getBody());
				responseObject.setError(false);
			}
			catch(HttpStatusCodeException exception) {
				responseObject.setResponseCode(exception.getStatusCode().value());
				responseObject.setError(true);
				responseObject.setResponseText((null==exception.getMessage())?"null":exception.getMessage());
				System.out.println("Response code:"+responseObject.getResponseCode());
				System.out.println("Response body:"+responseObject.getResponseText());
				throw new ProcessingException("Query processing failed!!!");
			}
			catch(Exception ex) {
				Log.error(ex.getMessage());
				throw new ProcessingException("Query processing failed!!!");
			}
		}
		return responseObject;		
	}
	
	
	public RequestObject parseRequest(Query query) {
		RequestObject requestObject = RequestObject.builder().query(query).build();
		
		
		return requestObject;
		
	}
	
	private static boolean isPathParamPresent(String url) {
		return url.contains("$");
	}
	
	
	private static void parseWhereClause(RequestObject requestObject) {
		String whereClause = requestObject.getQuery().getConditions();		
		if(!whereClause.isEmpty()) {
			whereClause = whereClause.replaceAll(" (?)and ", " AND ");
			whereClause = whereClause.replaceAll(" (?)or ", " OR ");
			String[] whereClauses =  whereClause.split("AND|OR");
			for(String where:whereClauses) {
				Condition condition = Condition.builder().build();
				condition.buildCondition(where);
				requestObject.add(condition);
			}
		}
		
	}
	
	
	private static void processPathParam(final RequestObject request) {
		Optional<SourceConfig> sourceConfigOptional = InMemoryConfigStore.fetchConfig(request.getQuery().getSource(),false);
		request.setUrl(sourceConfigOptional.get().getConfig(ConfigConstants.ENDPOINT));
		if(isPathParamPresent(request.getUrl())) {
			
			if(sourceConfigOptional.isPresent()) {
			String[] contexts = sourceConfigOptional.get().getConfig(ConfigConstants.ENDPOINT).split("/");
			
			Arrays.asList(contexts).stream()
			.filter(context->context.startsWith("$"))
			.forEach(context->{				
				request.addPathParams(context, getPathParam(request.getQuery(),context));
			});
			}
			
		}
	}
	
	private static String getPathParam(Query query, String key) {
		return "test";
	}
	
	
	public static List<LinkedList> filterResponseData(Query query,List<LinkedList> filteredResponseData ){
		
		String conditions = query.getConditions();
		return filteredResponseData;
		
	}
	
	

	public static List<Map<String,Object>> processResponseData(Query query, String response) throws JsonSqlException, JsonParseException, JsonMappingException, IOException{
		final List<Map<String,Object>> list = new ArrayList<>();
		if(!response.isEmpty() && response.charAt(0)=='[') {	
			CollectionType mapCollectionType = mapper.getTypeFactory().constructCollectionType(List.class, Map.class);
			List<Map<String,Object>> resultList = mapper.readValue(response, mapCollectionType);
			
			
			resultList.stream().map(map->{
				try {
					return mapper.writeValueAsString(map);
				} catch (JsonProcessingException e) {
					return "{}";
				}
			}).map(jsonText->{
				try {
					Map<String,Object> resultMap =  mapper.readValue(new JsonFlattener(jsonText).withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS).flatten(),Map.class);
					list.add(resultMap);
					return resultMap;
				} catch (IOException e) {
					return Collections.EMPTY_MAP;
				}
			}
			).count();
			
			System.out.println(list);
		}
		else if(!response.isEmpty()&& response.charAt(0)=='{') {
			
			Map<String, Object> flattenJson = mapper.readValue(new JsonFlattener(response).withFlattenMode(FlattenMode.KEEP_PRIMITIVE_ARRAYS).flatten(),Map.class);
			;
			list.add(flattenJson);
		}
		else {
			System.out.println("Not a valid json response!!");
		}
		
		return list;
		
	}
	

	
	
	 

	

	
	
	private static LinkedList addQueryColumnHeaders(Query query) {		
		Scanner sc = new Scanner(query.getColumns());
		sc.useDelimiter(",");
		LinkedList<String> headers= new LinkedList();
		while(sc.hasNext()) {
			headers.add(sc.next().trim());
		}
		return headers;
	}
	
	
	
	public static ResultSet processQueryResult(RequestObject request,List<Map<String,Object>> results, LinkedList columns ) {
		ResultSet resultSet = ResultSet.builder().build();
		List<String> allColumns = new ArrayList<>();
		results.forEach(map->{
			allColumns.addAll(map.keySet());
			});
		resultSet.setAllColumns(allColumns);
		results.forEach(row->{
			Map<String,Object> resultRowMap = new HashMap<>();
			allColumns.forEach(column->{
				if(row.containsKey(column)) {
					resultRowMap.put(column,(row.get(column)==null)?null:row.get(column).toString());
				}
				else {
					resultRowMap.put(column,null);
				}
			});
			
			resultSet.add(Row.builder().rowMap(resultRowMap).build());
		});

		return resultSet;
		
	}
	
	public static void applyConditionsToResult(ResultSet resultSet, RequestObject resquestObject) {
		
		
	}
	
	
	public static ConditionalLiteralWrapper getColumnLiteralWrapper (Row row,String conditionText) {
		if(isNumeric(conditionText)) {
			
			return ConditionalLiteralWrapper.builder()
					.conditionText(conditionText)
					.absoluteValue(conditionText)
					.type(Types.NUMBER).build();
					
		}
		else if(conditionText.startsWith("'") && conditionText.endsWith("'")) {
			return ConditionalLiteralWrapper.builder()
					.conditionText(conditionText)
					.absoluteValue(conditionText.substring(1, conditionText.length()-1))
					.type(Types.STRING).build();
		}
		else if(conditionText.equalsIgnoreCase("true")||conditionText.equalsIgnoreCase("false")) {
			return ConditionalLiteralWrapper.builder()
					.conditionText(conditionText)
					.absoluteValue(conditionText)
					.type(Types.BOOLEAN).build();
		}		
		return ConditionalLiteralWrapper.builder()
				.conditionText(conditionText)
				.absoluteValue(row.getRowMap().get(conditionText).toString())
				.type(Types.COLUMNVALUE).build();
	}
	
	
	
	public static boolean isNumeric(String text) {
	    if (text == null) {
	        return false; 
	    }
	    return pattern.matcher(text).matches();
	}
	
	
	
	public static void processConditions(ResultSet resultSet, RequestObject resquestObject) {
		List<Condition> conditions = resquestObject.getConditions();
		if(null!=conditions && !conditions.isEmpty())
			resultSet.getRows().stream()
				.map(row->{
					conditions.stream().forEach(condition->{
						if(condition.getOperator().equals("=")) {
							checkEqualsCondition(resquestObject,row,condition);
						}
					});
			return row;
		}).count();
		
	}
	public static void checkEqualsCondition(RequestObject resquestObject,Row row,Condition condition) {
		Map<String,Object> rowMap = row.getRowMap();
		if(!isReservedCondition(condition)) {
			ConditionalLiteralWrapper leftCondition = getColumnLiteralWrapper(row,condition.getLeft());
			ConditionalLiteralWrapper rightCondition = getColumnLiteralWrapper(row,condition.getRight());			
			if(!leftCondition.getAbsoluteValue().equals(rightCondition.getAbsoluteValue())){
				row.setDisabledByCondition(true);
			}
			
			
			
			
//		if(!fetchConditionValue(row,condition.getLeft(),resquestObject.getQuery().getSource())
//				.equals(fetchConditionValue(row,condition.getRight(),resquestObject.getQuery().getSource()))
//				) {
//			row.setDisabledByCondition(true);
//		}
		}
	}
	
	
	
	public static boolean isReservedConditionText(String text) {
		if(text.toLowerCase().startsWith("$header")||
				text.toLowerCase().startsWith("$cookies")||
				text.toLowerCase().startsWith("$path")||
				text.toLowerCase().startsWith("$query")
				) {
			return true;
			}
		return false;
	}
	
	public static boolean isReservedCondition(Condition condition) {
		if(isReservedConditionText(condition.getLeft())) {
			return true;
		}
		else if(isReservedConditionText(condition.getRight())) {
			return true;
		}
		return false;
	}
	
	public static boolean isMapColumn(String conditionText) {
		return conditionText.startsWith("$");		
	}
	
	

	
	
	public static Object fetchConditionValue(Row row, String conditionText, String aliasName) {
		if(isMapColumn(conditionText)) {
			return row.getRowMap().get(conditionText.subSequence(1, conditionText.length()).toString());
		}
		return conditionText;		
	}
	
	public static void processResultSetForProjection(ResultSet resultSet,RequestObject requestObject) {
		List<String> columns = Arrays.asList(requestObject.getQuery().getColumns().split(","));
		List<Row> processedRows = new ArrayList<>();
		resultSet.getRows().stream()
		.filter(row->!row.isDisabledByCondition())
		.forEach(row->{
			columns.forEach(column->{
				if(null==row.getProjectedMap()) {
					row.setProjectedMap(new HashMap<>());
				}
				row.getProjectedMap().put(column, row.getRowMap().get(column).toString());
			});	
			row.getRowMap().clear();
			processedRows.add(row);
		});
		resultSet.setRows(processedRows);
		resultSet.setProjectedColumns(columns);
		resultSet.getAllColumns().clear();
	}
	
	
	public static void print(ResultSet resultSet ) {
		printListInConsole(resultSet.getProjectedColumns());
		List<String> rowHolder = new ArrayList<>();
		resultSet.getRows().stream()
		.forEach(row->{			
			resultSet.getProjectedColumns().forEach(column->{
				rowHolder.add(row.getProjectedMap().get(column));
			});
			printListInConsole(rowHolder);
			rowHolder.clear();
		});	
	}
	public static void printListInConsole( List<String> columns) {
		int colNumbers = columns.size();
		StringBuffer format = new StringBuffer();
		for(int i=0;i<columns.size();i++) {
			format.append("%20s |");
		}
		String formatString = format.toString();
		System.out.println(String.format(formatString, columns.toArray()));
	}


public static void writeToCSV(ResultSet resultSet,RequestObject requestObject) throws JsonSqlException{
	try {
		FileWriter csvWriter = new FileWriter(requestObject.getQuery().getExport()+"/"+requestObject.getQuery().getSource()+"-"+Instant.now().toEpochMilli()+".csv");
		List<String> columns = resultSet.getProjectedColumns();
		writeCsvRow(columns,csvWriter);
		List<String> rowValues = new ArrayList<>();
		resultSet.getRows().forEach(row->{						
			columns.forEach(column->{
				if(null!=row.getProjectedMap().get(column)) {
				rowValues.add(row.getProjectedMap().get(column).toString());
				}
				else {
					rowValues.add("");
				}
			});
			try {
				writeCsvRow(rowValues,csvWriter);
				rowValues.clear();
			} catch (JsonSqlException e) {
				e.printStackTrace();
				System.out.println("Error while writing to CSV!!!");
			}
		});
		
		csvWriter.flush();
		csvWriter.close();
		
	} catch (IOException e) {
		e.printStackTrace();
		throw new ProcessingException("Error while Exporting to csv->"+
				requestObject.getQuery().getExport()+"/"+requestObject.getQuery().getSource()+"-"+Instant.now().toEpochMilli()+".csv");
	} 
	
}
public static void writeCsvRow(List<String> columns,FileWriter csvWriter) throws JsonSqlException {
	
	String row = columns.stream().collect(Collectors.joining(",")) +"\n";	
	try {
		csvWriter.append(row);
	} catch (IOException e) {
		e.printStackTrace();
		throw new ProcessingException("Error while Exporting to csv!!!!");
	}
}


}




