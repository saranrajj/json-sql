package json.sql.commands.constants;

public enum Operators {
EQUALS("="),
NOTEQUALS("!=");

	public final String value;
	
	private Operators(String value) {
        this.value = value;
    }
}
