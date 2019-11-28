package json.sql.commands.configstore;

import java.util.HashMap;
import java.util.Optional;

import org.jline.utils.Log;

import json.sql.commands.constants.ConfigConstants;
import json.sql.commands.model.SourceConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InMemoryConfigStore {
	
	public static HashMap<String, SourceConfig> sourceConfigs = new HashMap<>();
	
	
	 public static Optional<SourceConfig> fetchConfig(String key,boolean isCommand) {
		 Log.info("trying to fetch the alias for "+ key);
		 if(sourceConfigs.containsKey(key)) {
			 SourceConfig sourceConfig = sourceConfigs.get(key);
			 if(isCommand) {
				 System.out.println(sourceConfig);
			 }
			 return Optional.of(sourceConfig);
		 }		 
		 return Optional.empty();
	 }
	 
	 
	 public static void addSourceConfig(SourceConfig sourceConfig) {
		 Log.info("adding source config "+ sourceConfig);
		 sourceConfigs.put(sourceConfig.getConfig(ConfigConstants.ALIAS),sourceConfig);
	 }
	

}
