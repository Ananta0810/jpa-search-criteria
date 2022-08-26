package ananta.api.models;

import ananta.api.helpers.CollectionHelper;
import ananta.api.helpers.ReflectionHelper;
import ananta.api.helpers.StringHelper;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Joiner {
    private final List<JoinPoint> joinPoints = CollectionHelper.emptyList();
    
    private final Map<String, JoinPoint> joinPointMap = CollectionHelper.emptyMap();
    HashMap<String, From<?, ?>> joinMap = CollectionHelper.emptyMap();
    private JoinPoint rootJoin;
    private Root<?> root;
    
    public JoinPoint getRootJoin() {
        return rootJoin;
    }
    
    public Root<?> getRoot() {
        return root;
    }
    
    public void add(JoinPoint joinPoint) {
        boolean isJoinPoint = rootJoin == null && joinPoints.isEmpty();
        if (isJoinPoint) {
            putJoinPointToMap(joinPoint);
            rootJoin = joinPoint;
            return;
        }
        putJoinPointToMap(joinPoint);
        joinPoints.add(joinPoint);
    }
    
    private JoinPoint putJoinPointToMap(final JoinPoint joinPoint) {
        String tableName = joinPoint.getTableName();
        if (joinPointMap.containsKey(tableName)) {
            throw new QueryException("Table %s already declared.", tableName);
        }
        return joinPointMap.put(tableName, joinPoint);
    }
    
    public From<?, ?>  getJoin(String tableName) {
        return Optional
            .ofNullable(joinMap.get(tableName))
            .orElseThrow(() -> new QueryException("Can't find table %s", tableName));
    }
    
    
    public Optional<JoinPoint> getJoinPoint(String tableName) {
        if (StringHelper.isBlank(tableName)) {
            return Optional.ofNullable(rootJoin);
        }
        return Optional.ofNullable(joinPointMap.get(tableName));
    }
    
    
    public JoinPoint getLast() {
        return joinPoints.isEmpty() ? rootJoin : CollectionHelper.getLastElementOf(joinPoints);
    }
    
    public void initJoinMap(CriteriaQuery<?> query) {
        joinMap.clear();
        
        Root<?> root = query.from(this.rootJoin.getClazz());
        this.root = root;
        From<?, ?> lastJoin = root;
        Class<?> lastClass = this.rootJoin.getClazz();
        
        joinMap.put(this.rootJoin.getTableName(), root);
        
        for (JoinPoint joinPoint : joinPoints) {
            Class<?> finalLastClass = lastClass;
            
            String fieldName = Optional.ofNullable(joinPoint.getField())
                .map(Field::getName)
                .orElseGet(() -> getJoinField(finalLastClass, joinPoint.getClazz()).getName());
            
            Join<?, ?> newJoin = lastJoin.join(fieldName);
            joinMap.put(joinPoint.getTableName(), newJoin);
            
            lastJoin = newJoin;
            lastClass = joinPoint.getClazz();
        }
    }
    
    
    private <LEFT, RIGHT> Field getJoinField(final Class<LEFT> lastClass, final Class<RIGHT> newTableClass) {
        List<Field> fields = ReflectionHelper.getFieldsOf(lastClass);
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
