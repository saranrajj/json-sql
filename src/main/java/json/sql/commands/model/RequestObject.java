package json.sql.commands.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@Builder

public class RequestObject {
	
	Query query;
	boolean valid;
	String errorMessage;
	String url;
	Map<String,String> queryParams;
	Map<String,String> pathParams;
	Map<String,String> headers;
	Map<String,String> cookies;
	List<Condition> conditions;
	
	
	public void addPathParams(String key,String value) {
		pathParams.put(key, value);
	}
	
	public void addQueryParams(String key,String value) {
		queryParams.put(key, value);
	}
	
	public void addheaders(String key,String value) {
		if(headers==null) {
			headers = new HashMap<>();
		}
		headers.put(key, value);
	}
	
	public void addCookies(String key,String value) {
		if(cookies==null) {
			cookies = new HashMap<>();
		}
		cookies.put(key, value);
	}
	
	public void add(Condition condition) {
		if(conditions==null) {
			conditions = new ArrayList<Condition> ();
		}
		conditions.add(condition);
	}
	
	
	public String getProcessedUrl() {
		return getQueryParamText();
		
	}
	
	
	public String getPathParamUrl() {
		AtomicReference<String> sourceRef = new AtomicReference(this.url);
		if(null!=conditions) {
		conditions.stream().filter(condition->condition.getLeft().startsWith("$path.")).forEach(condition->{
			String toReplaceKey = condition.getLeft().replace("$path.", "$");
			sourceRef.set(sourceRef.get().replace(toReplaceKey, condition.getRight()));
			
		});
		}
		return sourceRef.get();		
	}
	
	public String getQueryParamText() {
		AtomicReference<String> sourceRef = new AtomicReference(this.getPathParamUrl());
		if(null!=conditions) {		
		conditions.stream().filter(condition->condition.getLeft().startsWith("$query.")).forEach(condition->{
			String toReplaceKey = condition.getLeft().replace("$query.", "");
			if(!sourceRef.get().contains("?")) {
				sourceRef.set(sourceRef.get()+"?"+toReplaceKey+"="+condition.getRight());
			}
			else {
				sourceRef.set(sourceRef.get()+"&"+toReplaceKey+"="+condition.getRight());
			}						
		});
		}
		return sourceRef.get();
	}
	
	public void getHeaderParams() {		
		if(null!=conditions) {		
		conditions.stream().filter(condition->condition.getLeft().startsWith("$header.")).forEach(condition->{
			String headerKey = condition.getLeft().replace("$header.", "");
			addheaders(headerKey,condition.getRight());		
		});
		}
	}
	
	public void getCookiesParams() {		
		if(null!=conditions) {		
		conditions.stream().filter(condition->condition.getLeft().startsWith("$cookies.")).forEach(condition->{
			String cookiesKey = condition.getLeft().replace("$cookies.", "");
			addCookies(cookiesKey,condition.getRight());		
		});
		}
	}
	
	public void parseHeadParams() {
		getHeaderParams();
		getCookiesParams();
		
	}
	
}
