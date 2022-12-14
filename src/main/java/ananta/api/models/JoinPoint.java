package ananta.api.models;

import ananta.api.helpers.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.Optional;

public class JoinPoint {
    private final String tableName;
    private final Class<?> clazz;
    private final Field field;
    
    private JoinPoint(final Builder builder) {
        tableName = builder.tableName;
        clazz = builder.clazz;
        field = builder.field;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Class<?> getClazz() {
        return clazz;
    }
    
    public Optional<Class<?>> getType() {
        if (field == null) {
            return Optional.ofNullable(clazz);
        }
        return ReflectionHelper.getTypeOf(field);
    }
    
    public Field getField() {
        return field;
    }
    
    public static final class Builder {
        private String tableName;
        private Class<?> clazz;
        private Field field;
        
        private Builder() {
        }
        
        public Builder tableName(final String val) {
            tableName = val;
            return this;
        }
        
        public Builder clazz(final Class<?> val) {
            clazz = val;
            return this;
        }
        
        public Builder field(final Field val) {
            field = val;
            return this;
        }
        
        public JoinPoint build() {
            return new JoinPoint(this);
        }
    }
}
