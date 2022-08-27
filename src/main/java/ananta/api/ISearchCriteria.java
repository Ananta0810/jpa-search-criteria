package ananta.api;

import ananta.api.statics.ForAll;
import ananta.api.statics.ForCollection;
import ananta.api.statics.ForNumber;
import ananta.api.statics.ForString;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ISearchCriteria<T, ROOT> {
    ISearchCriteria<T, ROOT> from(Class<ROOT> clazz);
    ISearchCriteria<T, ROOT> from(Class<ROOT> clazz, String as);
    ISearchCriteria<T, ROOT> from(String tableName);
    ISearchCriteria<T, ROOT> from(String tableName, String as);
    
    ISearchCriteria<T, ROOT> join(Class<?> clazz);
    ISearchCriteria<T, ROOT> join(Class<?> clazz, String as);
    ISearchCriteria<T, ROOT> join(String tableName);
    ISearchCriteria<T, ROOT> join(String tableName, String as);
    
    ISearchCriteria<T, ROOT> where(String key, ForAll action, Object value);
    ISearchCriteria<T, ROOT> where(String key, ForString action, String value);
    <NUMBER extends Comparable<? super NUMBER>> ISearchCriteria<T, ROOT> where(String key, ForNumber action, NUMBER value);
    ISearchCriteria<T, ROOT> where(String key, ForCollection action, Collection<? extends Serializable> value);
    
    ISearchCriteria<T, ROOT> and(String key, ForAll action, Object value);
    ISearchCriteria<T, ROOT> and(String key, ForString action, String value);
    <NUMBER extends Comparable<? super NUMBER>> ISearchCriteria<T, ROOT> and(String key, ForNumber action, NUMBER value);
    ISearchCriteria<T, ROOT> and(String key, ForCollection action, Collection<? extends Serializable> value);
    
    ISearchCriteria<T, ROOT> or(String key, ForAll action, Object value);
    ISearchCriteria<T, ROOT> or(String key, ForString action, String value);
    <NUMBER extends Comparable<? super NUMBER>> ISearchCriteria<T, ROOT> or(String key, ForNumber action, NUMBER value);
    ISearchCriteria<T, ROOT> or(String key, ForCollection action, Collection<? extends Serializable> value);
    
    
    ISearchCriteria<T, ROOT> withPage(Pageable page);
    ISearchCriteria<T, ROOT> withPage(int pageNumber, int size, String orderBy, boolean isAscending);
    
    List<T> toList();
    Set<T> toSet();
    Page<T> toPage();
    Optional<T> findFirst();
    
    Long count();
    boolean existAny();
}
