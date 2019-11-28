package json.sql.commands.constants;

public enum Operations {
	
	AND("and"),
	OR("or");
	
	public final String value;
	
	private Operations(String value) {
        this.value = value;
    }

}
