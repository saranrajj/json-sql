package json.sql.commands;

import java.util.HashMap;
import java.util.Map;

import org.jline.utils.Log;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import json.sql.commands.configstore.InMemoryConfigStore;
import json.sql.commands.constants.ConfigConstants;
import json.sql.commands.exception.JsonSqlException;
import json.sql.commands.exception.ValidationException;
import json.sql.commands.model.Query;
import json.sql.commands.model.SourceConfig;
import json.sql.commands.parser.ConfigValidator;
import json.sql.commands.parser.QueryValidator;
import json.sql.commands.processor.QueryProcessor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@ShellComponent
public class JsonSqlCommands
{
	 @ShellMethod("select query")
	    public String select(
	    		String columns,
	    		String from,
	    		String source,
	    		@ShellOption(defaultValue = "") String where,
	    		@ShellOption(defaultValue = "") String condition,
	    		@ShellOption(defaultValue = "",value = {"export"}) String export
	    		) {
		 try {
		 Log.info("invoking the select method!!!");
		 Query query =Query.builder().columns(columns)
				 .source(source)
				 .where(where)
				 .conditions(condition)
				 .export(export)
				 .build();
		 
		 QueryValidator.builder().query(query).build().validateQuery();
		 
		 QueryProcessor.executeQuery(query);
	      // process the query		 		 
	      return "Query execution completed!!!";
		 }
		 catch(JsonSqlException je) {
			 Log.error(je.getMessage());
		 }
		 return "";
	    }
	 
	 
	 
	 @ShellMethod("add config")
	 public String config( 
			 @ShellOption(defaultValue = "")String command,
			 @ShellOption(defaultValue = "")String alias,
			 @ShellOption(defaultValue = "") String source
			 ) {
		 try {
		 Log.info("invoking the config method!!!");
		 if(command.equalsIgnoreCase("add")) {
		 Map<String,String> configMap = new HashMap<String,String>();		 
			 SourceConfig sourceConfig = new SourceConfig(configMap);
			 sourceConfig.addConfig(ConfigConstants.ALIAS, alias);
			 sourceConfig.addConfig(ConfigConstants.ENDPOINT, source);
			 ConfigValidator.validateSourceConfig(sourceConfig);	
			 InMemoryConfigStore.addSourceConfig(sourceConfig);
		 return alias+ " alias created successfully!!!";
		 }
		 else if(command.equalsIgnoreCase("get")&&!alias.isEmpty()) {
			 InMemoryConfigStore.fetchConfig(alias,true);
		 }
		 else {
			 throw new ValidationException("config command is not valid!!!");
		 }
		 }
		 catch(JsonSqlException je) {
			 log.error(je.getMessage());
		 }
		 return "";
		 
	 }
	 



}


