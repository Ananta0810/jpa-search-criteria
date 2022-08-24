package ananta.api.models;

import org.slf4j.helpers.MessageFormatter;

public class QueryException extends RuntimeException {
    public QueryException(String message) {
        super(message);
    }
    
    public QueryException(String message, Object... args) {
        super(MessageFormatter.arrayFormat(message, args).getMessage());
    }
}
