package ananta.api.models;

public class QueryException extends RuntimeException {
    public QueryException(String message) {
        super(message);
    }
    
    public QueryException(String message, Object... args) {
        super(String.format(message, args));
    }
}
