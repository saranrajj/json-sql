package json.sql.commands.exception;

public abstract class JsonSqlException extends Exception{
	 public JsonSqlException(String errorMessage) {
	        super(errorMessage);
	    }
}
