package ananta.api.models;

import ananta.api.statics.ForNumber;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class WhereClauseForNumber implements WhereClause {
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    private final TableJoin table;
    private final ForNumber action;
    private final Comparable value;
    
    private WhereClauseForNumber(final Builder builder) {
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
        Expression<? extends Comparable> expression = join.get(table.getColumn());
        
        switch (action) {
            case LESS_THAN:
                return cb.lessThan(expression, value);
            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo(expression, value);
            case LARGER_THAN:
                return cb.greaterThan(expression, value);
            case LARGER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo(expression, value);
        }
        throw QUERY_TYPE_NOT_ACCEPTED;
    }
    
    public static final class Builder {
        private TableJoin table;
        private ForNumber action;
        private Comparable value;
        
        private Builder() {
        }
        
        public Builder table(final TableJoin val) {
            table = val;
            return this;
        }
        
        public Builder action(final ForNumber val) {
            action = val;
            return this;
        }
        
        public Builder value(final Comparable val) {
            value = val;
            return this;
        }
        
        public WhereClauseForNumber build() {
            return new WhereClauseForNumber(this);
        }
    }
}
