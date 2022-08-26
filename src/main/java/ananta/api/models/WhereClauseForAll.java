package ananta.api.models;

import ananta.api.statics.ForAll;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class WhereClauseForAll implements WhereClause{
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    
    private final TableJoin table;
    private final ForAll action;
    private final Object value;
    
    private WhereClauseForAll(final Builder builder) {
        table = builder.table;
        action = builder.action;
        value = builder.value;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @Override
    public TableJoin getTable() {
        return table;
    }
    
    @Override
    public Predicate getPredicate(final CriteriaBuilder cb, From<?, ?> join) {
        Expression<Object> expression = join.get(table.getColumn());
        
        switch (action) {
            case EQUAL:
                return cb.equal(expression, value);
            case NOT_EQUAL:
                return cb.notEqual(expression, value);
        }
        throw QUERY_TYPE_NOT_ACCEPTED;
    }
    
    public static final class Builder {
        private TableJoin table;
        private ForAll action;
        private Object value;
        
        private Builder() {
        }
        
        public Builder table(final TableJoin val) {
            table = val;
            return this;
        }
        
        public Builder action(final ForAll val) {
            action = val;
            return this;
        }
        
        public Builder value(final Object val) {
            value = val;
            return this;
        }
        
        public WhereClauseForAll build() {
            return new WhereClauseForAll(this);
        }
    }
}