package ananta.api.models;

import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Field;

@Builder
@Getter
public class JoinPoint {
    private final String tableName;
    private final Class<?> clazz;
    private final Field field;
}
