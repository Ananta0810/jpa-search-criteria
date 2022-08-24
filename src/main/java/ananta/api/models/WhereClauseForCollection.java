package ananta.api.models;

import ananta.api.statics.ForCollection;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import java.io.Serializable;
import java.util.Collection;

@Builder
@Getter
public class WhereClauseForCollection implements WhereClause{
    private static final QueryException QUERY_TYPE_NOT_ACCEPTED = new QueryException("Action not accepted.");
    private final TableJoin table;
    private final ForCollection action;
    private final Collection<? extends Serializable> value;
    
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
}
