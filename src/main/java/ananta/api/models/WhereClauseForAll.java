package ananta.api.models;

import ananta.api.statics.ForAll;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

@Builder
@Getter
public class WhereClauseForAll implements WhereClause{
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    
    private final TableJoin table;
    private final ForAll action;
    private final Object value;
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
}