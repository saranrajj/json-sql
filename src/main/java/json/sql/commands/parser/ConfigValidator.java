package json.sql.commands.parser;

import org.jline.utils.Log;

import json.sql.commands.constants.ConfigConstants;
import json.sql.commands.exception.JsonSqlException;
import json.sql.commands.exception.ValidationException;
import json.sql.commands.model.SourceConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConfigValidator {
	
	public static void validateSourceConfig(SourceConfig sourceconfig) throws JsonSqlException{
		Log.info("invoking validateSourceConfig method");
		Log.info("sourceconfig:"+sourceconfig);
		if(sourceconfig.getConfig(ConfigConstants.ALIAS).isEmpty()) {
			throw new ValidationException("Alias name is missing!!!");
		}
		else if(sourceconfig.getConfig(ConfigConstants.ENDPOINT).isEmpty()) {
			throw new ValidationException("source is missing!!!");
		}
				
	}
	
}