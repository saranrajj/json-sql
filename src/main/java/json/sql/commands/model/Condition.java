package json.sql.commands.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class Condition {
	private String left;
	private String right;
	private String operator;
	
	public boolean isPathParam(String literal) {
		if(literal.startsWith("$path.")) {
			return true;
		}
		return false;
	}
	
	public boolean isHeaderParam(String literal) {
		if(literal.startsWith("$header.")) {
			return true;
		}
		return false;
	}
	public boolean isQueryParam(String literal) {
		if(literal.startsWith("$query.")) {
			return true;
		}
		return false;
	}
	
	
	
	public Condition buildCondition(String condition) {
		String[] literals = condition.split("=|>|<",2);
		if(condition.contains("=")) {
			this.operator = "=";
		}
		else if(condition.contains(">")) {
			this.operator = ">";
		}
		else if(condition.contains("<")) {
			this.operator = "<";
		}
		if(literals.length ==2 ) {
			if(!isPathParam(literals[1].trim())
			   &&!isHeaderParam(literals[1].trim())
			   &&!isQueryParam(literals[1].trim())) {
			this.left = literals[0].trim();
			this.right = literals[1].trim();
			}
			else {
				this.left = literals[1].trim();
				this.right = literals[0].trim();
			}
			
		}
		return this;
	}

}
