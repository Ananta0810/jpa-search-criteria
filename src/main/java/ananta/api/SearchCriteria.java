package ananta.api;

import ananta.api.helpers.*;
import ananta.api.models.*;
import ananta.api.statics.ForAll;
import ananta.api.statics.ForCollection;
import ananta.api.statics.ForNumber;
import ananta.api.statics.ForString;
import com.google.gson.Gson;
import org.springframework.data.domain.*;

import javax.persistence.*;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class SearchCriteria<T, ROOT> implements ISearchCriteria<T, ROOT> {
    private static EntityManager em;
    private static Map<String, Class<?>> entities;
    private final CriteriaBuilder cb;
    private final CriteriaQuery<Object[]> query;
    private final Class<T> returnType;
    private final List<QueryClause> predicates = CollectionHelper.emptyList();
    private final ananta.api.models.Joiner joiner = new ananta.api.models.Joiner();
    private Pageable page;
    
    public static void init(final EntityManager entityManager) {
        TypeHelper.checkNull(entityManager, "Entity manager should not be null.");
    
        final Set<Class<?>> entityClasses = entityManager.getMetamodel()
            .getEntities()
            .stream()
            .map(entity -> {
                final List<Field> fields = ReflectionHelper.getNonStaticFieldsOf(entity.getJavaType());
                final List<Class<?>> classes = fields.stream().filter(CriteriaHelper::isMappingColumn).map(CriteriaHelper::getEntityOf).collect(Collectors.toList());
                classes.add(entity.getJavaType());
                return classes;
            })
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());
    
        em = entityManager;
        entities = CollectionHelper.mapOf(entityClasses, CriteriaHelper::tableNameOf);
    }

    public static void configGson(final Gson gson) {
        TypeHelper.checkNull(gson, "gson should not be null.");
        TypeHelper.configGson(gson);
    }
    
    private SearchCriteria(final Class<T> clazz) {
        TypeHelper.checkNull(em, "Entity manager have not been initialized. Please call init method.");
        this.cb = em.getCriteriaBuilder();
        this.query = this.cb.createQuery(Object[].class);
        this.returnType = clazz;
    }

    public static <T, R> SearchCriteria<T, R> select(final Class<T> clazz) {
        TypeHelper.checkNull(clazz, "Can't select null class.");
        return new SearchCriteria<>(clazz);
    }
    
    @SuppressWarnings("unchecked")
    public static <T> SearchCriteria<T, T> selectFrom(final Class<T> clazz) {
        TypeHelper.checkNull(clazz, "Can't select null class.");
        return (SearchCriteria<T, T>) new SearchCriteria<>(clazz).from((Class<Object>) clazz);
    }
    
    @Override
    public SearchCriteria<T, ROOT> from(final Class<ROOT> clazz) {
        final String tableName = CriteriaHelper.getTableNameOf(clazz);
        final JoinPoint joinPoint = JoinPoint.builder().clazz(clazz).tableName(tableName).build();
        this.joiner.add(joinPoint);
        return this;
    }
    @Override
    public SearchCriteria<T, ROOT> from(final Class<ROOT> clazz, final String as) {
        final JoinPoint joinPoint = JoinPoint.builder().clazz(clazz).tableName(as).build();
        this.joiner.add(joinPoint);
        
        return this;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public SearchCriteria<T, ROOT> from(final String tableName) {
        TypeHelper.checkNull(tableName, "Table name must not be null.");
        final Class<ROOT> rootClass = (Class<ROOT>) this.getTableClass(tableName);
        
        return this.from(rootClass, tableName);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public SearchCriteria<T, ROOT> from(final String tableName, final String as) {
        TypeHelper.checkNull(tableName, "Table name must not be null.");
        final Class<ROOT> rootClass = (Class<ROOT>) this.getTableClass(tableName);
        return this.from(rootClass, as);
    }
    
    private Class<?> getTableClass(final String tableName) {
        return entities.get(tableName);
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(final Class<?> clazz) {
        final String tableName = CriteriaHelper.getTableNameOf(clazz);
        final JoinPoint joinPoint = JoinPoint.builder().tableName(tableName).clazz(clazz).build();
        this.joiner.add(joinPoint);
        
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(final Class<?> clazz, final String as) {
        final JoinPoint joinPoint = JoinPoint.builder().tableName(as).clazz(clazz).build();
        this.joiner.add(joinPoint);
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(final String tableName) {
        return this.joinUsingTableName(tableName, tableName);
    }
    
    @Override
    public SearchCriteria<T, ROOT> join(final String tableName, final String as) {
        return this.joinUsingTableName(tableName, as);
    }
    
    private SearchCriteria<T, ROOT> joinUsingTableName(final String tableName, final String as) {
        final Class<?> entity = this.getTableClass(tableName);
        if (entity != null) {
            return this.join(entity, as);
        }
        
        final Field field = this.getFieldWithJoinTable(tableName).orElseThrow(() -> new QueryException("Table %s not found.", tableName));
        final JoinPoint joinPoint = JoinPoint.builder().tableName(as).field(field).build();
        this.joiner.add(joinPoint);
        
        return this;
    }
    
    private Optional<Field> getFieldWithJoinTable(final String tableName) {
        final Class<?> lastClazz = this.joiner.getLast().getClazz();
        return ReflectionHelper
            .getNonStaticFieldsOf(lastClazz).stream()
            .filter(field -> {
                if (Objects.equals(field.getName(), tableName)) {
                    return true;
                }
                final Optional<JoinTable> joinTableAnnotation = ReflectionHelper.getAnnotation(JoinTable.class, field);
                if (joinTableAnnotation.isPresent()) {
                    return Objects.equals(joinTableAnnotation.get().name(), tableName);
                }
                
                final Optional<JoinColumn> joinColumnAnnotation = ReflectionHelper.getAnnotation(JoinColumn.class, field);
                if (joinColumnAnnotation.isPresent()) {
                    return Objects.equals(joinColumnAnnotation.get().name(), tableName);
                }
                
                final Optional<ManyToMany> manyToManyAnnotation = ReflectionHelper.getAnnotation(ManyToMany.class, field);
                if (manyToManyAnnotation.isPresent()) {
                    return Objects.equals(manyToManyAnnotation.get().mappedBy(), tableName);
                }
                return false;
            })
            .findFirst();
    }
    
    @Override
    public SearchCriteria<T, ROOT> where(final String key, final ForAll action, final Object value) {
        this.checkKeyAndAction(key, action);
        
        if (value != null) {
            final TableJoin table = this.getTableJoinOf(key);
            final WhereClause clause = WhereClauseForAll.builder()
                .table(table)
                .action(action)
                .value(value)
                .build();
            this.predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
        }
        return this;
    }
    
    private TableJoin getTableJoinOf(final String key) {
        final TableJoin table = TableJoin.of(key);
        
        final JoinPoint joinPoint = this.joiner
            .getJoinPoint(table.getName())
            .orElseThrow(() -> new QueryException("Can't find table %s", table.getName()));
    
        final Class<?> clazz = joinPoint.getType().orElseThrow(() -> new NullPointerException("Join point have no class inside."));
    
        final String fieldName = this.getJoinField(clazz, table.getColumn())
            .map(Field::getName)
            .orElseThrow(() -> new QueryException("Can't find column %s in %s", table.getColumn(), joinPoint.getTableName()));
        
        return table.withColumn(fieldName);
    }
    
    private Optional<Field> getJoinField(final Class<?> clazz, final String column) {
        return ReflectionHelper
            .getNonStaticFieldsOf(clazz).stream()
            .filter(field -> {
                if (Objects.equals(field.getName(), column)) {
                    return true;
                }
                return ReflectionHelper.getAnnotation(Column.class, field)
                    .map(col -> Objects.equals(col.name(), column))
                    .orElse(false);
            }).findFirst();
    }
    
    private String getTableNameFrom(final TableJoin table) {
        return table.getNameOrElse(this.joiner.getRootJoin().getTableName());
    }
    
    @Override
    public SearchCriteria<T, ROOT> where(final String key, final ForString action, final String value) {
        this.checkKeyAndAction(key, action);
        
        if (StringHelper.isNotBlank(value)) {
            final WhereClause clause = WhereClauseForString.builder()
                .table(this.getTableJoinOf(key))
                .action(action)
                .value(value)
                .build();
            this.predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
            
        }
        return this;
    }
    @Override
    public <NUMBER extends Comparable<? super NUMBER>> SearchCriteria<T, ROOT> where(final String key, final ForNumber action, final NUMBER value) {
        this.checkKeyAndAction(key, action);
        if (value != null) {
            final WhereClause clause = WhereClauseForNumber.builder()
                .table(this.getTableJoinOf(key))
                .action(action)
                .value(value)
                .build();

            this.predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
        }
        return this;
    }
    @Override
    public SearchCriteria<T, ROOT> where(final String key, final ForCollection action, final Collection<? extends Serializable> value) {
        this.checkKeyAndAction(key, action);
        
        if (CollectionHelper.isNotEmpty(value)) {
            final WhereClause clause = WhereClauseForCollection.builder()
                .table(this.getTableJoinOf(key))
                .action(action)
                .value(value)
                .build();

            this.predicates.add(QueryClause.builder().isAndClause(true).clause(clause).build());
        }
        return this;
    }
    
    private void checkKeyAndAction(final String key, final Object action) {
        TypeHelper.checkNull(key, "Key must not be null.");
        TypeHelper.checkNull(action, "Action must not be null.");
    }
    
    @Override
    public SearchCriteria<T, ROOT> and(final String key, final ForAll action, final Object value) {
        return this.where(key, action, value);
    }
    @Override
    public SearchCriteria<T, ROOT> and(final String key, final ForString action, final String value) {
        return this.where(key, action, value);
    }
    @Override
    public <NUMBER extends Comparable<? super NUMBER>> SearchCriteria<T, ROOT> and(final String key, final ForNumber action, final NUMBER value) {
        return this.where(key, action, value);
    }
    @Override
    public SearchCriteria<T, ROOT> and(final String key, final ForCollection action, final Collection<? extends Serializable> value) {
        return this.where(key, action, value);
    }
    
    @Override
    public SearchCriteria<T, ROOT> or(final String key, final ForAll action, final Object value) {
        this.checkKeyAndAction(key, action);
        
        if (value != null) {
            final WhereClause clause = WhereClauseForAll.builder()
                .table(this.getTableJoinOf(key))
                .action(action)
                .value(value)
                .build();
            this.predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> or(final String key, final ForString action, final String value) {
        this.checkKeyAndAction(key, action);
        
        if (StringHelper.isNotBlank(value)) {
            final WhereClause clause = WhereClauseForString.builder()
                .table(this.getTableJoinOf(key))
                .action(action)
                .value(value)
                .build();

            this.predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public <NUMBER extends Comparable<? super NUMBER>> SearchCriteria<T, ROOT> or(final String key, final ForNumber action, final NUMBER value) {
        this.checkKeyAndAction(key, action);
        
        if (value != null) {
            final WhereClause clause = WhereClauseForNumber.builder()
                .table(this.getTableJoinOf(key))
                .action(action)
                .value(value)
                .build();
            this.predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> or(final String key, final ForCollection action, final Collection<? extends Serializable> value) {
        this.checkKeyAndAction(key, action);
        
        if (value != null) {
            final WhereClause clause = WhereClauseForCollection.builder()
                .table(this.getTableJoinOf(key))
                .action(action)
                .value(value)
                .build();
            this.predicates.add(QueryClause.builder().isAndClause(false).clause(clause).build());
        }
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> withPage(final Pageable page) {
        this.page = page;
        return this;
    }
    
    @Override
    public SearchCriteria<T, ROOT> withPage(final int pageNumber, final int size, final String orderBy, final boolean isAscending) {
        TypeHelper.checkNull(orderBy, "Order by must not be null.");
        
        final Sort.Direction sort = isAscending ? Sort.Direction.ASC : Sort.Direction.DESC;
        int page = pageNumber - 1;
        if (page < 0) {
            page = 0;
        }
        
        this.page = PageRequest.of(page, size, sort, orderBy);
        return this;
    }
    
    @Override
    public List<T> toList() {
        final List<String> fields = this.getSelectFields();
        this.joiner.initJoinMap(this.query);

        this.query.multiselect(this.getSelections(fields));
        this.getPredicate(this.cb).ifPresent(this.query::where);
        
        final List<Object[]> tuple = em.createQuery(this.query).getResultList();
        return tuple.stream().map(objectValues -> this.getObjectFrom(fields, objectValues)).collect(Collectors.toList());
    }
    
    private Optional<Predicate> getPredicate(final CriteriaBuilder cb) {
        Predicate whereClause = null;
        for (final QueryClause predicate : this.predicates) {
            final String tableName = this.getTableNameFrom(predicate.getClause().getTable());
            final From<?, ?> join = this.joiner.getJoin(tableName);
            
            if (whereClause == null) {
                final WhereClause clause = predicate.getClause();
                whereClause = clause.getPredicate(cb, join);
                continue;
            }
            if (predicate.isAndClause()) {
                whereClause = cb.and(whereClause, predicate.getClause().getPredicate(cb, join));
                continue;
            }
            whereClause = cb.or(whereClause, predicate.getClause().getPredicate(cb, join));
        }
        return Optional.ofNullable(whereClause);
    }
    
    @Override
    public Set<T> toSet() {
        return CollectionHelper.setOf(this.toList());
    }
    @Override
    public Page<T> toPage() {
        if (this.page == null) {
            throw new QueryException("Pageable undefined.");
        }
        final List<String> fields = this.getSelectFields();
        this.joiner.initJoinMap(this.query);

        this.query.multiselect(this.getSelections(fields));
        this.getPredicate(this.cb).ifPresent(this.query::where);
    
        final List<Object[]> tuple = em.createQuery(this.query)
            .setFirstResult((int) this.page.getOffset())
            .setMaxResults(this.page.getPageSize())
            .getResultList();
        
        final List<T> items = tuple.stream()
            .map(objectValues -> this.getObjectFrom(fields, objectValues))
            .collect(Collectors.toList());
        
        return new PageImpl<>(items, this.page, this.count());
    }
    
    @Override
    public Optional<T> findFirst() {
        try {
            final List<String> fields = this.getSelectFields();
            this.joiner.initJoinMap(this.query);

            this.query.multiselect(this.getSelections(fields));
            this.getPredicate(this.cb).ifPresent(this.query::where);
        
            final Object[] value = em.createQuery(this.query).getSingleResult();
            return Optional.ofNullable(this.getObjectFrom(fields, value));
        } catch (final NoResultException exception) {
            return Optional.empty();
        }
    }
    
    @Override
    public Long count() {
        final CriteriaQuery<Long> countQuery = em.getCriteriaBuilder().createQuery(Long.class);

        this.joiner.initJoinMap(countQuery);
        
        final Expression<Long> selection = this.cb.count(this.cb.literal(1));
        countQuery.multiselect(selection);

        this.getPredicate(this.cb).ifPresent(this.query::where);
        return em.createQuery(countQuery).getSingleResult();
    }
    
    @Override
    public boolean existAny() {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        this.joiner.initJoinMap(query);
        
        query.select(this.cb.literal(1L));
        this.getPredicate(this.cb).ifPresent(this.query::where);
        
        final List<Long> result = em.createQuery(query).setMaxResults(1).getResultList();
        return CollectionHelper.isNotEmpty(result);
    }
    
    private Selection<?>[] getSelections(final List<String> fields) {
        return fields.stream().map(field -> this.joiner.getRoot().get(field)).toArray(Selection[]::new);
    }
    
    private List<String> getSelectFields() {
        return ReflectionHelper
            .getNonStaticFieldsOf(this.returnType).stream()
            .filter(CriteriaHelper::isColumn)
            .map(Field::getName).collect(Collectors.toList());
    }
    
    private T getObjectFrom(final List<String> fields, final Object[] values) {
        final HashMap<String, Object> fieldValueMap = CollectionHelper.emptyMap();
        for (int i = 0; i < fields.size() - 1; ++i) {
            final String field = fields.get(i);
            final Object value = values[i];
            fieldValueMap.put(field, value);
        }
        return TypeHelper.convertFromMapToObject(this.returnType, fieldValueMap);
    }
}