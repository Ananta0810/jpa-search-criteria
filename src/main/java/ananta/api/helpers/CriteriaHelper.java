package ananta.api.helpers;

import ananta.api.models.QueryException;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ClassUtils;
import org.jetbrains.annotations.NotNull;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Predicate;

@UtilityClass
public class CriteriaHelper {
    private static final Set<Class<? extends Annotation>> RELATIONSHIP_ANNOTATIONS = Set.of(ManyToMany.class, ManyToOne.class, OneToMany.class, OneToOne.class, JoinColumn.class);
    
    public static String getTableNameOf(final Class<?> clazz) {
        return ReflectionHelper
            .getAnnotation(Table.class, clazz)
            .map(Table::name)
            .orElseThrow(
                () -> new QueryException("Your class {} must annotated with @Table annotation.", clazz.getSimpleName())
            );
    }
    
    public static boolean isColumn(@NotNull final Field field) {
        Class<?> type = field.getType();
        boolean isPrimitiveType = ClassUtils.isPrimitiveOrWrapper(type);
        if (isPrimitiveType || type.isAssignableFrom(String.class)) {
            return true;
        }
        if (ReflectionHelper.isCollection(field)) {
            return false;
        }
        Predicate<Annotation> isMappingAnnotation = ann -> RELATIONSHIP_ANNOTATIONS.contains(ann.annotationType());
        ArrayList<Annotation> annotations = ReflectionHelper.getAnnotationsOf(field);
        return annotations.stream().noneMatch(isMappingAnnotation);
    }
    
    public static boolean isMappingColumn(@NotNull final Field field) {
        Class<?> type = field.getType();
        Predicate<Annotation> isMappingAnnotation = ann -> RELATIONSHIP_ANNOTATIONS.contains(ann.annotationType());
        ArrayList<Annotation> annotations = ReflectionHelper.getAnnotationsOf(field);
        return annotations.stream().anyMatch(isMappingAnnotation);
    }
    
    @NotNull
    public static Class<?> getEntityOf(final Field field) {
        if (!ReflectionHelper.isCollection(field)) {
            return field.getType();
        }
        Type genericType = field.getGenericType();
        return ReflectionHelper.genericTypeOf(genericType).orElseThrow(() -> new QueryException("Can't find entity of field {} in {}", field.getName()));
    }
    
    
    public static String tableNameOf(Class<?> entity) {
        return ReflectionHelper
            .getAnnotation(Table.class, entity)
            .map(Table::name)
            .orElseThrow(() -> new QueryException("There is entity that does not have @Table annotation."));
    }
}
