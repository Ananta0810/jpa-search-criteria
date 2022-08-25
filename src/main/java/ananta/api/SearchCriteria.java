package ananta.api;

import ananta.api.helpers.CollectionHelper;
import ananta.api.helpers.CriteriaHelper;
import ananta.api.helpers.ReflectionHelper;
import ananta.api.models.*;
import ananta.api.statics.ForAll;
import ananta.api.statics.ForCollection;
import ananta.api.statics.ForNumber;
import ananta.api.statics.ForString;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Selection;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class SearchCriteria<T, ROOT> implements ISearchCriteria<T, ROOT> {
    
    private static final Gson gson = new Gson();
    private static EntityManager em;
    private static Map<String, Class<?>> entities;
    private final CriteriaBuilder cb;
    private final CriteriaQuery<Object[]> query;
    private final Class<T> returnType;
    private final List<QueryClause> predicates = Lists.newArrayList();
    private final Joiner joiner = new Joiner();
    private Pageable page;
    
    public static void init(EntityManager entityManager) {
        Preconditions.checkNotNull(entityManager, "Entity manager should not be null.");
    
        List<Class<?>> entityClasses = entityManager.getMetamodel()
            .getEntities()
            .stream()
            .map(entity -> {
                List<Field> fields = ReflectionHelper.getNonStaticFieldsOf(entity.getJavaType());
                List<Class<?>> classes = fields.stream().filter(CriteriaHelper::isMappingColumn).map(CriteriaHelper::getEntityOf).collect(Collectors.toList());
                classes.add(entity.getJavaType());
                return classes;
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    
        em = entityManager;
        entities = CollectionHelper.mapOf(entityClasses, CriteriaHelper::tableNameOf);
    }
    
    private SearchCriteria(Class<T> clazz) {
        Preconditions.checkNotNull(em, "Entity manager have not been initialized. Please call init method.");
        this.cb = em.getCriteriaBuilder();
        this.query = cb.createQuery(Object[].class);
        this.returnType = clazz;
    }
    
    
    public static <T, R> SearchCriteria<T, R> select(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Can't select null class.");
        return new SearchCriteria<>(clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> SearchCriteria<T, T> selectFrom(Class<T> clazz) {
        Preconditions.checkNotNull(clazz, "Can't select null class.");
        return (SearchCriteria<T, T>) new SearchCriteria<>(clazz).from((Class<Object>) clazz);
    }
    
    @Override
    public SearchCriteria<T, ROOT> from(Class<ROOT> clazz) {
        String tableName = CriteriaHelper.getTableNameOf(clazz);
        JoinPoint joinPoint = JoinPoint.builder().clazz(clazz).tableName(tableName).build();
        joiner.add(joinPoint);
        return this;
    }
    @Override
    public SearchCriteria<T, ROOT> from(Class<ROOT> clazz, String as) {
        JoinPoint joinPoint = JoinPoint.builder().clazz(clazz).tableName(as).build();
        joiner.add(joinPoint);
        
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public SearchCriteria<T, ROOT> from(String tableName) {
        Preconditions.checkNotNull(tableName, "Table name must not be null.");
        Class<ROOT> rootClass = (Class<ROOT>) getTableClass(tableName);
        
        return from(rootClass, tableName);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public SearchCriteria<T, ROOT> from(String tableName, String as) {
        Preconditions.checkNotNull(tableName, "Table name must not be null.");
        Class<ROOT> rootClass = (Class<ROOT>) getTableClass(tableName);
        return from(rootClass, as);
    }
    
    private Class<?> getTableClass(final String tableName) {
        return entities.get(tableName);
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(Class<?> clazz) {
        String tableName = CriteriaHelper.getTableNameOf(clazz);
        JoinPoint joinPoint = JoinPoint.builder().tableName(tableName).clazz(clazz).build();
        joiner.add(joinPoint);
        
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(Class<?> clazz, String as) {
        JoinPoint joinPoint = JoinPoint.builder().tableName(as).clazz(clazz).build();
        joiner.add(joinPoint);
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(String tableName) {
        Class<?> entity = getTableClass(tableName);
        if (entity != null) {
            return join(entity, tableName);
        }
    
        Field field = getFieldWithJoinTable(tableName).orElseThrow(() -> new QueryException("Table not found."));
        JoinPoint joinPoint = JoinPoint.builder().tableName(tableName).field(field).build();
        joiner.add(joinPoint);
    
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(String tableName, String as) {
        Class<?> entity = getTableClass(tableName);
        if (entity != null) {
            return join(entity, as);
        }
    
        Field field = getFieldWithJoinTable(tableName).orElseThrow(() -> new QueryException("Table not found."));
        JoinPoint joinPoint = JoinPoint.builder().tableName(as).field(field).build();
        joiner.add(joinPoint);
    
        return this;
    }
    
    @NotNull
    private Optional<Field> getFieldWithJoinTable(final String tableName) {
        Class<?> lastClazz = joiner.getLast().getClazz();
        return ReflectionHelper
            .getNonStaticFieldsOf(lastClazz).stream()
            .filter(field -> {
                if (Objects.equals(field.getName(), tableName)) {
                    return true;
                }
                Optional<JoinTable> joinTableAnnotation = ReflectionHelper.getAnnotation(JoinTable.class, field);
                Optional<JoinColumn> joinColumnAnnotation = ReflectionHelper.getAnnotation(JoinColumn.class, field);
                Optional<ManyToMany> manyToManyAnnotation = ReflectionHelper.getAnnotation(ManyToMany.class, field);
                if (joinTableAnnotation.isPresent()) {
                    return Objects.equals(joinTableAnnotation.get().name(), tableName);
                }
                if (joinColumnAnnotation.isPresent()) {
                    return Objects.equals(joinColumnAnnotation.get().name(), tableName);
                }
                if (manyToManyAnnotation.isPresent()) {
                    return Objects.equals(manyToManyAnnotation.get().mappedBy(), tableName);
                }
                return false;
            })
            .findFirst();
    }
    
    @Override
    public SearchCriteria<T, ROOT> where(String key, ForAll action, Object value) {
        checkKeyAndAction(key, action);
        TableJoin table = TableJoin.of(key);
        
        if (value != null) {
            WhereClause clause = WhereClauseForAll.builder()
                .table(table)
                .action(action)
                .value(value)
                .build();
            predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
        }
        return this;
    }
    
    private String getTableNameFrom(final TableJoin table) {
        return table.getNameOrElse(joiner.getRootJoin().getTableName());
    }
    
    @Override
    public SearchCriteria<T, ROOT> where(String key, ForString action, String value) {
        checkKeyAndAction(key, action);
        
        if (Strings.isNotBlank(value)) {
            WhereClause clause = WhereClauseForString.builder()
                .table(TableJoin.of(key))
                .action(action)
                .value(value)
                .build();
            predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
            
        }
        return this;
    }
    @Override
    public <NUMBER extends Comparable<? super NUMBER>> SearchCriteria<T, ROOT> where(String key, ForNumber action, NUMBER value) {
        checkKeyAndAction(key, action);
        if (value != null) {
            WhereClause clause = WhereClauseForNumber.builder()
                .table(TableJoin.of(key))
                .action(action)
                .value(value)
                .build();
            
            predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
        }
        return this;
    }
    @Override
    public SearchCriteria<T, ROOT> where(String key, ForCollection action, Collection<? extends Serializable> value) {
        checkKeyAndAction(key, action);
        
        if (CollectionUtils.isNotEmpty(value)) {
            WhereClause clause = WhereClauseForCollection.builder()
                .table(TableJoin.of(key))
                .action(action)
                .value(value)
                .build();
            
            predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
        }
        return this;
    }
    
    private void checkKeyAndAction(final String key, final Object action) {
        Preconditions.checkNotNull(key, "Key must not be null.");
        Preconditions.checkNotNull(action, "Action must not be null.");
    }
    
    @Override
    public SearchCriteria<T, ROOT> and(String key, ForAll action, Object value) {
        // TODO: Fix timePattern not found. Must use pattern
        return where(key, action, value);
    }
    @Override
    public SearchCriteria<T, ROOT> and(String key, ForString action, String value) {
        return where(key, action, value);
    }
    @Override
    public <NUMBER extends Comparable<? super NUMBER>> SearchCriteria<T, ROOT> and(String key, ForNumber action, NUMBER value) {
        return where(key, action, value);
    }
    @Override
    public SearchCriteria<T, ROOT> and(String key, ForCollection action, Collection<? extends Serializable> value) {
        return where(key, action, value);
    }
    
    @Override
    public SearchCriteria<T, ROOT> or(final String key, final ForAll action, final Object value) {
        checkKeyAndAction(key, action);
        
        if (value != null) {
            WhereClause clause = WhereClauseForAll.builder()
                .table(TableJoin.of(key))
                .action(action)
                .value(value)
                .build();
            predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> or(final String key, final ForString action, final String value) {
        checkKeyAndAction(key, action);
        
        if (Strings.isNotBlank(value)) {
            WhereClause clause = WhereClauseForString.builder()
                .table(TableJoin.of(key))
                .action(action)
                .value(value)
                .build();
            
            predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public <NUMBER extends Comparable<? super NUMBER>> SearchCriteria<T, ROOT> or(final String key, final ForNumber action, final NUMBER value) {
        checkKeyAndAction(key, action);
        
        if (value != null) {
            WhereClause clause = WhereClauseForNumber.builder()
                .table(TableJoin.of(key))
                .action(action)
                .value(value)
                .build();
            predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> or(final String key, final ForCollection action, final Collection<? extends Serializable> value) {
        checkKeyAndAction(key, action);
        
        if (value != null) {
            WhereClause clause = WhereClauseForCollection.builder()
                .table(TableJoin.of(key))
                .action(action)
                .value(value)
                .build();
            predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> withPage(Pageable page) {
        this.page = page;
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> withPage(int pageNumber, int size, String orderBy, boolean isAscending) {
        Preconditions.checkNotNull(orderBy, "Order by must not be null.");
        
        Sort.Direction sort = isAscending ? Sort.Direction.ASC : Sort.Direction.DESC;
        int page = pageNumber - 1;
        if (page < 0) {
            page = 0;
        }
        
        this.page = PageRequest.of(page, size, sort, orderBy);
        return this;
    }
    
    @Override
    public List<T> toList() {
        List<String> fields = getSelectFields();
        joiner.initJoinMap(query);
        
        query.multiselect(getSelections(fields)).where(getPredicate(cb));
        
        List<Object[]> tuple = em.createQuery(query).getResultList();
        return tuple.stream().map(objectValues -> getObjectFrom(fields, objectValues)).collect(Collectors.toList());
    }
    
    private Predicate getPredicate(final CriteriaBuilder cb) {
        Predicate whereClause = null;
        for (QueryClause predicate : predicates) {
            String tableName = getTableNameFrom(predicate.getClause().getTable());
            From<?, ?> join = joiner.getJoin(tableName);
            
            if (whereClause == null) {
                WhereClause clause = predicate.getClause();
                whereClause = clause.getPredicate(cb, join);
                continue;
            }
            if (predicate.isAndClause()) {
                whereClause = cb.and(whereClause, predicate.getClause().getPredicate(cb, join));
                continue;
            }
            whereClause = cb.or(whereClause, predicate.getClause().getPredicate(cb, join));
        }
        return whereClause;
    }
    
    @Override
    public Set<T> toSet() {
        return Sets.newHashSet(toList());
    }
    @Override
    public Page<T> toPage() {
        if (page == null) {
            throw new QueryException("Pageable undefined.");
        }
        List<String> fields = getSelectFields();
        joiner.initJoinMap(query);
        
        query.multiselect(getSelections(fields)).where(getPredicate(cb));
        
        List<Object[]> tuple = em.createQuery(query)
            .setFirstResult((int) page.getOffset())
            .setMaxResults(page.getPageSize())
            .getResultList();
        
        List<T> items = tuple.stream()
            .map(objectValues -> getObjectFrom(fields, objectValues))
            .collect(Collectors.toList());
        
        Long total = countTotalResultFound();
        
        return new PageImpl<>(items, page, total);
    }
    
    private Long countTotalResultFound() {
        CriteriaQuery<Long> countQuery = em.getCriteriaBuilder().createQuery(Long.class);
        joiner.initJoinMap(countQuery);
        Expression<Long> selection = cb.count(cb.literal(1));
        countQuery.multiselect(selection).where(getPredicate(cb));
        return em.createQuery(countQuery).getSingleResult();
    }
    
    @Override
    public Optional<T> findFirst() {
        List<String> fields = getSelectFields();
        joiner.initJoinMap(query);
        
        query.multiselect(getSelections(fields)).where(getPredicate(cb));
        Object[] value = em.createQuery(query).getSingleResult();
        return Optional.ofNullable(getObjectFrom(fields, value));
    }
    
    @NotNull
    private Selection<?>[] getSelections(final List<String> fields) {
        return fields.stream().map(field -> joiner.getRoot().get(field)).toArray(Selection[]::new);
    }
    
    @NotNull
    private List<String> getSelectFields() {
        return ReflectionHelper
            .getNonStaticFieldsOf(returnType).stream()
            .filter(CriteriaHelper::isColumn)
            .map(Field::getName).collect(Collectors.toList());
    }
    
    private T getObjectFrom(final List<String> fields, final Object[] values) {
        HashMap<String, Object> fieldValueMap = Maps.newHashMap();
        for (int i = 0; i < fields.size() - 1; ++i) {
            String field = fields.get(i);
            Object value = values[i];
            fieldValueMap.put(field, value);
        }
        return convertFromMapToObject(fieldValueMap);
    }
    
    private T convertFromMapToObject(final HashMap<String, Object> valueMap) {
        JsonElement jsonElement = gson.toJsonTree(valueMap);
        return gson.fromJson(jsonElement, returnType);
    }
}