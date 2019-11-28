package json.sql.commands.model;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class Row {
	
	Map<String,Object> rowMap;
	boolean disabledByCondition;
	Map<String,String> projectedMap;
	
	
	

}
