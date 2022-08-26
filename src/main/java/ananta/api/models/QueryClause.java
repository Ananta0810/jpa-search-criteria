package ananta.api.models;

public class QueryClause {
    private final boolean isAndClause;
    private final WhereClause clause;
    
    private QueryClause(final Builder builder) {
        isAndClause = builder.isAndClause;
        clause = builder.clause;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    
    public boolean isAndClause() {
        return isAndClause;
    }
    
    public WhereClause getClause() {
        return clause;
    }
    
    public static final class Builder {
        private boolean isAndClause;
        private WhereClause clause;
        
        private Builder() {
        }
        
        public Builder isAndClause(final boolean val) {
            isAndClause = val;
            return this;
        }
        
        public Builder clause(final WhereClause val) {
            clause = val;
            return this;
        }
        
        public QueryClause build() {
            return new QueryClause(this);
        }
    }
}
