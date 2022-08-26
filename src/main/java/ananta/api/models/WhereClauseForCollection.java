package ananta.api.models;

import ananta.api.statics.ForCollection;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.Collection;

public class WhereClauseForCollection implements WhereClause{
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    private final TableJoin table;
    private final ForCollection action;
    private final Collection<? extends Serializable> value;
    
    private WhereClauseForCollection(final Builder builder) {
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
            case IN:
                return expression.in(value);
//            case NOT_IN:
//                return cb.in(expression, value);
        }
        throw QUERY_TYPE_NOT_ACCEPTED;
    }
    
    
    public static final class Builder {
        private TableJoin table;
        private ForCollection action;
        private Collection<? extends Serializable> value;
        
        private Builder() {
        }
        
        public Builder table(final TableJoin val) {
            table = val;
            return this;
        }
        
        public Builder action(final ForCollection val) {
            action = val;
            return this;
        }
        
        public Builder value(final Collection<? extends Serializable> val) {
            value = val;
            return this;
        }
        
        public WhereClauseForCollection build() {
            return new WhereClauseForCollection(this);
        }
    }
}
