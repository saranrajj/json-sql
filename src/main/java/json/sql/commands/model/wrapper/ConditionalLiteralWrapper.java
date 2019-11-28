package json.sql.commands.model.wrapper;

import json.sql.commands.constants.Types;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class ConditionalLiteralWrapper {
	
	public String conditionText;
	public String absoluteValue;
	public Types type;

}
