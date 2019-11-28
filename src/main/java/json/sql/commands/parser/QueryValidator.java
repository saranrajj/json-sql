package json.sql.commands.parser;

import java.io.File;
import java.util.Optional;

import org.jline.utils.Log;

import json.sql.commands.configstore.InMemoryConfigStore;
import json.sql.commands.exception.JsonSqlException;
import json.sql.commands.exception.ValidationException;
import json.sql.commands.model.Query;
import json.sql.commands.model.SourceConfig;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
@Getter
@Setter
@ToString
@Builder
@Slf4j
public class QueryValidator {

	private Query query;
	
	
	public void validateQuery() throws JsonSqlException{
		validateColumns();
		validateSource();
		validateConditions();	
		validateExportPath();
	}
	
	private void validateColumns() {
		//
	}
	
	private void validateExportPath() throws JsonSqlException{
		if(!query.getExport().isEmpty()) {
		File directory = new File(query.getExport());
		if(!directory.exists()) {
			throw new ValidationException("Export Directory "+ query.getExport()+" does not exist!!!");
		}
		}
	}
	
	private void validateSource() throws JsonSqlException{
		Optional<SourceConfig> sourceConfigOptional = InMemoryConfigStore.fetchConfig(query.getSource(),false);
		if(!sourceConfigOptional.isPresent()) {
			throw new ValidationException("Alias Source \'"+ query.getSource()+"\' not found - add to config!!!");
		}
		
	}
	
	private void validateConditions() throws JsonSqlException{
		if(query.getWhere().isEmpty()&&!query.getConditions().isEmpty()) {
			throw new ValidationException("where clause is missing in the query!!!");
		}
		else if(!query.getWhere().isEmpty()&&!query.getWhere().equalsIgnoreCase("where")) {
			throw new ValidationException("expecting where clause!!!");
		}
		else if(!query.getWhere().isEmpty()&&query.getConditions().isEmpty()) {
			throw new ValidationException("conditions are missing after where clause!!!");
		}
	}
	
}