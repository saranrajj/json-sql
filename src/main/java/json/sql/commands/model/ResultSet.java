package json.sql.commands.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class ResultSet {
	
	List<Row> rows;
	List<String> allColumns;
	List<String> projectedColumns;
	
	public void add(Row row) {
		if(rows==null) {
			rows = new ArrayList<>();
		}
		rows.add(row);
	}

}
