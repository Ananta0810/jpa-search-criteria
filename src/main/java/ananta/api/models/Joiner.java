package ananta.api.models;

import ananta.api.helpers.CollectionHelper;
import ananta.api.helpers.ReflectionHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Joiner {
    private final List<JoinPoint> joinPoints = Lists.newArrayList();
    
    @Getter
    private JoinPoint rootJoin;
    HashMap<String, From<?, ?>> joinMap = Maps.newHashMap();
    @Getter
    private Root<?> root;
    
    public void add(JoinPoint joinPoint) {
        boolean isJoinPoint = rootJoin == null && joinPoints.isEmpty();
        if (isJoinPoint) {
            rootJoin = joinPoint;
            return;
        }
        joinPoints.add(joinPoint);
    }
    
    public From<?, ?>  getJoin(String tableName) {
        return Optional
            .ofNullable(joinMap.get(tableName))
            .orElseThrow(() -> new QueryException("Can't find table {}", tableName));
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
            .orElseThrow(() -> new QueryException("Can't find the field to join between {} and {}.", lastClass.getSimpleName(), newTableClass.getSimpleName()));
    }
}
