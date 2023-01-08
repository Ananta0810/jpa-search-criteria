package ananta.api.models;

import ananta.api.helpers.CollectionHelper;
import ananta.api.helpers.ReflectionHelper;
import ananta.api.helpers.StringHelper;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.*;

public class Joiner {
    private final List<JoinPoint> joinPoints = CollectionHelper.emptyList();
    
    private final Map<String, JoinPoint> joinPointMap = CollectionHelper.emptyMap();
    HashMap<String, From<?, ?>> joinMap = CollectionHelper.emptyMap();
    private JoinPoint rootJoin;
    private Root<?> root;
    
    public JoinPoint getRootJoin() {
        return this.rootJoin;
    }
    
    public Root<?> getRoot() {
        return this.root;
    }
    
    public void add(final JoinPoint joinPoint) {
        final boolean isJoinPoint = this.rootJoin == null && this.joinPoints.isEmpty();
        if (isJoinPoint) {
            this.putJoinPointToMap(joinPoint);
            this.rootJoin = joinPoint;
            return;
        }
        this.putJoinPointToMap(joinPoint);
        this.joinPoints.add(joinPoint);
    }
    
    private void putJoinPointToMap(final JoinPoint joinPoint) {
        final String tableName = joinPoint.getTableName();
        if (this.joinPointMap.containsKey(tableName)) {
            throw new QueryException("Table %s already declared.", tableName);
        }
        this.joinPointMap.put(tableName, joinPoint);
    }
    
    public From<?, ?>  getJoin(final String tableName) {
        return Optional
            .ofNullable(this.joinMap.get(tableName))
            .orElseThrow(() -> new QueryException("Can't find table %s", tableName));
    }
    
    
    public Optional<JoinPoint> getJoinPoint(final String tableName) {
        if (StringHelper.isBlank(tableName)) {
            return Optional.ofNullable(this.rootJoin);
        }
        return Optional.ofNullable(this.joinPointMap.get(tableName));
    }
    
    
    public JoinPoint getLast() {
        return this.joinPoints.isEmpty() ? this.rootJoin : CollectionHelper.getLastElementOf(this.joinPoints);
    }
    
    public void initJoinMap(final CriteriaQuery<?> query) {
        this.joinMap.clear();
        
        final Root<?> root = query.from(this.rootJoin.getClazz());
        this.root = root;
        From<?, ?> lastJoin = root;
        Class<?> lastClass = this.rootJoin.getClazz();

        this.joinMap.put(this.rootJoin.getTableName(), root);
        
        for (final JoinPoint joinPoint : this.joinPoints) {
            final Class<?> finalLastClass = lastClass;
            
            final String fieldName = Optional.ofNullable(joinPoint.getField())
                .map(Field::getName)
                .orElseGet(() -> this.getJoinField(finalLastClass, joinPoint.getClazz()).getName());
            
            final Join<?, ?> newJoin = lastJoin.join(fieldName);
            this.joinMap.put(joinPoint.getTableName(), newJoin);
            
            lastJoin = newJoin;
            lastClass = joinPoint.getClazz();
        }
    }
    
    
    private <LEFT, RIGHT> Field getJoinField(final Class<LEFT> lastClass, final Class<RIGHT> newTableClass) {
        final List<Field> fields = ReflectionHelper.getFieldsOf(lastClass);
        return fields.stream()
            .filter(field -> {
                if (Objects.equals(field.getType(), newTableClass)) {
                    return true;
                }
                if (ReflectionHelper.isCollection(field)) {
                    return ReflectionHelper.genericTypeOf(field.getGenericType()).map(newTableClass::equals).orElse(false);
                }
                return false;
            })
            .findFirst()
            .orElseThrow(() -> new QueryException("Can't find the field to join between %s and %s.", lastClass.getSimpleName(), newTableClass.getSimpleName()));
    }
}
