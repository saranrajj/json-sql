package json.sql.commands.model;

import java.util.Map;


public class SourceConfig {
	public SourceConfig(Map<String,String> config){
		this.config = config;
		
	}
	
	private  Map<String,String> config;	
	
	
	public void addConfig(String key, String value) {		
		this.config.put(key, value);		
	}
	
	
	public String getConfig(String key) {
		return this.config.get(key);
	}
	
	public boolean isConfigSet(String key) {
		return this.config.containsKey(key);
	}
	
	public String toString() {		
		return this.config.toString();
	}
	
	
}
