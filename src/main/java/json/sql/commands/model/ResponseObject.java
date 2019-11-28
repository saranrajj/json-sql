package json.sql.commands.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class ResponseObject {
	
	String responseText;
	String errorMessage;
	boolean isError;
	int responseCode;
	Map<String,String> headers;
	
	public void addHeaders(String key, String value) {
		if(null==headers) {
			headers = new HashMap<>();
			headers.put(key, value);
		}
	}
	

}
