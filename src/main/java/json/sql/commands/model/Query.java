package json.sql.commands.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class Query {
	public String columns;
	public String source;
	public String where;
	public String conditions;
	public String export;
}
