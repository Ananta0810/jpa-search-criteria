package ananta.api.models;

import ananta.api.statics.ForNumber;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

@Builder
@Getter
public class WhereClauseForNumber implements WhereClause {
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    private final TableJoin table;
    private final ForNumber action;
    private final Comparable value;
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
}
