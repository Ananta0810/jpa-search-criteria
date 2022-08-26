package ananta.api.models;

import ananta.api.statics.ForString;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public class WhereClauseForString implements WhereClause {
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    private final TableJoin table;
    private final ForString action;
    private final String value;
    
    private WhereClauseForString(final Builder builder) {
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
        Expression<String> expression = join.get(table.getColumn());
        switch (action) {
            case LIKE:
                return cb.like(expression, "%" + value + "%");
            case NOT_LIKE:
                return cb.notLike(expression, "%" + value + "%");
//            case LIKE_IGNORE_CASE:
//                return cb.notLike(join.get(column), "%" + value + "%");
            case START_WITH:
                return cb.like(expression, value + "%");
//            case START_WITH_IGNORE_CASE:
//                return cb.like(join.get(column), value + "%");
            case NOT_START_WITH:
                return cb.notLike(expression, value + "%");
            case END_WITH:
                return cb.like(expression, "%" + value);
//            case END_WITH_IGNORE_CASE:
//                return cb.like(join.get(column), "%" + value);
            case NOT_END_WITH:
                return cb.notLike(expression, "%" + value);
        }
        throw QUERY_TYPE_NOT_ACCEPTED;
    }
    
    public static final class Builder {
        private TableJoin table;
        private ForString action;
        private String value;
        
        private Builder() {
        }
        
        public Builder table(final TableJoin val) {
            table = val;
            return this;
        }
        
        public Builder action(final ForString val) {
            action = val;
            return this;
        }
        
        public Builder value(final String val) {
            value = val;
            return this;
        }
        
        public WhereClauseForString build() {
            return new WhereClauseForString(this);
        }
    }
}
