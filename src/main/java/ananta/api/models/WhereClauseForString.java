package ananta.api.models;

import ananta.api.statics.ForString;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

@Builder
@Getter
public class WhereClauseForString implements WhereClause {
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    private final TableJoin table;
    private final ForString action;
    private final String value;
    
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
}
