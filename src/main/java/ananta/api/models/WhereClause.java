package ananta.api.models;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;

public interface WhereClause {
    
    TableJoin getTable();
    Predicate getPredicate(final CriteriaBuilder cb, From<?, ?> join);
}
