package ananta.api.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class JoinPoint {
    private final String tableName;
    private final Class<?> clazz;
}
