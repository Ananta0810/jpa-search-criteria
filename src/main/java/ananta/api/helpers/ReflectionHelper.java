package ananta.api.helpers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReflectionHelper {
    
    private ReflectionHelper() {}
    
    private static final Set<Class<?>> wrapperClasses = CollectionHelper.setOf(Boolean.class, Byte.class, Character.class, Double.class, Float.class, Integer.class, Long.class, Short.class, Void.class);
    
    /**
     * @author Ananta0810
     * Check if a class is extended from other class.
     * @param parentClass can be null.
     * @param clazz can be null.
     * @return true if the class extends or implements the parent class or the input classes is null, otherwise false.
     */
    public static boolean isChildClassOf(Class<?> parentClass, Class<?> clazz) {
        if (Objects.isNull(parentClass) || Objects.isNull(clazz)) {
            return false;
        }
        List<Class<?>> classes = getAncestorClasses(clazz);
        return classes.stream().anyMatch(c -> c.isAssignableFrom(parentClass));
    }
    
    /**
     * @author Ananta0810
     * Get all fields name of a class. Those fields including private and public fields in
     * that one class and its ancestors.
     * @param clazz The class that you want to extract. Class can be null.
     * @return empty list if class is null. Otherwise, return list of field's name.
     */
    public static List<String> getFieldNamesOf(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return CollectionHelper.emptyList();
        }
        return getFieldsOf(clazz).stream().map(Field::getName).collect(Collectors.toList());
    }
    
    /**
     * Check if a field is exists in certain class or not.
     * This method will ignore case while checking.
     * @param fieldName can be null.
     * @param clazz can be null.
     * @return false if inputs are null or when field is not exist in the class. Otherwise, return true.
     */
    public static boolean existField(String fieldName, Class<?> clazz) {
        if (fieldName == null || clazz == null) {
            return false;
        }
        return getFieldNamesOf(clazz).stream().anyMatch(field -> field.equalsIgnoreCase(fieldName));
    }
    
    /**
     * @author Ananta0810
     * Get all fields name of a class. Those fields including private and public fields in
     * that one class and its ancestors.
     * @param clazz The class that you want to extract. Class can be null.
     * @return empty set if class is null. Otherwise, return set of field's name.
     */
    public static Set<String> getFieldNameSetOf(Class<?> clazz) {
        return CollectionHelper.setOf(getFieldNamesOf(clazz));
    }
    
    /**
     * @author Ananta0810
     * Get all fields of a class. Those fields including private and public fields in
     * that one class and its ancestors.
     * @param clazz The class that you want to extract. Class can be null.
     * @return empty list if class is null, otherwise return its fields.
     */
    public static List<Field> getFieldsOf(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return CollectionHelper.emptyList();
        }
        List<Class<?>> classes = getAncestorClasses(clazz);
        return classes.stream().map(Class::getDeclaredFields).flatMap(Stream::of).collect(Collectors.toList());
    }
    
    /**
     * @author Ananta0810
     * Get all non-static fields of a class. Those fields including private and public fields in
     * that one class and its ancestors.
     * @param clazz The class that you want to extract. Class can be null.
     * @return empty list if class is null, otherwise return its fields.
     */
    public static List<Field> getNonStaticFieldsOf(Class<?> clazz) {
        if (Objects.isNull(clazz)) {
            return CollectionHelper.emptyList();
        }
        List<Class<?>> classes = getAncestorClasses(clazz);
        return classes.stream().map(Class::getDeclaredFields).flatMap(Stream::of).filter(field -> !Modifier.isStatic(field.getModifiers())).collect(Collectors.toList());
    }
    
    /**
     * @author Ananta0810
     * Find field of a class based on its name (ignore case).
     * @param fieldName name of the field. If null then return Optional.empty()
     * @param clazz class which contains the field. If null then return Optional.empty().
     * @return Empty if input is null or can't find the field.
     */
    public static Optional<Field> getField(String fieldName, Class<?> clazz) {
        if (Objects.isNull(fieldName)|| Objects.isNull(clazz)) {
            return Optional.empty();
        }
        final List<Field> fields = getFieldsOf(clazz);
        return fields.stream().filter(field -> field.getName().equalsIgnoreCase(fieldName)).findFirst();
    }
    
    /**
     * @author Ananta0810
     * Get field's value of a object.
     * @param field field of the object. Field can be null.
     * @param object object that contains the value. Object can be null.
     * @return Optional.empty() if input is null or field can not be accessed. Otherwise, return Optional of object.
     */
    public static Optional<?> getFieldValue(Field field, Object object) {
        if (Objects.isNull(field) || Objects.isNull(object)){
            return Optional.empty();
        }
        
        final boolean canAccess = field.canAccess(object);
        if (!canAccess) {
            field.setAccessible(true);
        }
        Object value;
        try {
            value = field.get(object);
        } catch (IllegalAccessException e) {
            value = null;
        } finally {
            field.setAccessible(canAccess);
        }
        return Optional.ofNullable(value);
    }
    
    /**
     * @author Ananta0810
     * Get all annotations of a field as a list.
     * @param fieldName name of the field to extract. Field can be null.
     * @param clazz Class that contains the field. Class can be null.
     * @return Empty list if input are null or field not found. Otherwise, return list of annotations found.
     */
    public static List<Annotation> getFieldAnnotations(String fieldName, Class<?> clazz) {
        if (Objects.isNull(fieldName)|| Objects.isNull(clazz)) {
            return List.of();
        }
        return getField(fieldName, clazz).map(field -> List.of(field.getAnnotations())).orElseGet(List::of);
    }
    
    /**
     * @author Ananta0810
     * Get all annotations of a class and its ancestors.
     * @param clazz The class that you want to extract. Class can be null.
     * @return empty list if class is null. Otherwise, return annotations of class.
     */
    public static List<Annotation> getAnnotationsOf(Class<?> clazz) {
        if (Objects.isNull(clazz)){
            return CollectionHelper.emptyList();
        }
        List<Class<?>> classes = getAncestorClasses(clazz);
        return classes.stream().map(Class::getAnnotations).flatMap(Stream::of).collect(Collectors.toList());
    }
    
    /**
     * Get all annotations of a field
     * @param field can be null.
     * @return empty list if field is null. Otherwise, return annotations of the field.
     */
    public static List<Annotation> getAnnotationsOf(final Field field) {
        if (field == null) {
            return CollectionHelper.emptyList();
        }
        return CollectionHelper.listOf(field.getDeclaredAnnotations());
    }
    
    /**
     * Get certain annotation of a class.
     * @param annotationClass Annotation you want to find. Can't be null.
     * @param clazz Class that you want to get annotation from. Can't be null.
     * @return Empty Optional if not found. Otherwise, return Optional of annotation.
     */
    public static <T> Optional<T> getAnnotation(final Class<T> annotationClass, final Class<?> clazz) {
        return getAnnotationsOf(clazz).stream()
            .filter(ann -> ann.annotationType().isAssignableFrom(annotationClass))
            .map(annotationClass::cast)
            .findFirst();
    }
    
    /**
     * Get certain annotation of a field.
     * @param annotationClass Annotation you want to find. Can't be null.
     * @param field Field that you want to get annotation from. Can't be null.
     * @return Empty Optional if not found. Otherwise, return Optional of annotation.
     */
    public static <T> Optional<T> getAnnotation(final Class<T> annotationClass, final Field field) {
        return Arrays.stream(field.getAnnotations())
            .filter(ann -> ann.annotationType().isAssignableFrom(annotationClass))
            .map(annotationClass::cast)
            .findFirst();
    }
    
    /**
     * @author Ananta0810
     * Get all ancestors of a class including the class itself.
     * @param clazz The class that you want to extract. Class can be null.
     * @throws IllegalArgumentException If input class is null.
     */
    public static List<Class<?>> getAncestorClasses(Class<?> clazz) {
        TypeHelper.checkNull(clazz, "Can't find ancestors of a null.");
        
        List<Class<?>> classes = CollectionHelper.emptyList();
        Class<?> tempClass = clazz;
        while (tempClass != null) {
            classes.add(tempClass);
            tempClass = tempClass.getSuperclass();
        }
        return classes;
    }
    
    /**
     * @author Ananta0810
     * Check if a class has annotation or not.
     * @param annotationClass can be null.
     * @param clazz can be null.
     * @return false if input is null or class doesn't have annotation. Otherwise, return true.
     */
    public static boolean hasAnnotation(Class<? extends Annotation> annotationClass, Class<?> clazz) {
        if (Objects.isNull(annotationClass) || Objects.isNull(clazz)) {
            return false;
        }
        List<Annotation> annotations = getAnnotationsOf(clazz);
        return annotations.stream().anyMatch(annotation -> annotation.annotationType().equals(annotationClass));
    }
    
    /**
     * Check if certain field contains the annotation or not.
     * @param annotationClass can be null.
     * @param field can be null.
     * @return false if any input is null or field not contains annotation. Otherwise, return true.
     */
    public static boolean hasAnnotation(final Class<? extends Annotation> annotationClass, final Field field) {
        if (annotationClass == null || field == null) {
            return false;
        }
        return field.getAnnotation(annotationClass) != null;
    }
    
    
    /**
     * @author Ananta0810
     * Check if annotation is defined in certain package.
     * @param annotation can be null.
     * @param packageName can be null.
     * @return false if input is null or if annotation is not in the package. Otherwise, return true.
     */
    public static boolean isOfPackage(Annotation annotation, String packageName) {
        if (Objects.isNull(annotation) || Objects.isNull(packageName)){
            return false;
        }
        return annotation.annotationType().getPackageName().equals(packageName);
    }
    
    /**
     * @author Ananta0810
     * Set field value for object.
     * @param object can be null
     * @param fieldName can be null
     * @param fieldValue can be null
     * @return true if set value successfully. Otherwise, return false.
     * If return false, it might be due to some follow reasons:<br/>
     * - Object or field name is null<br/>
     * - Field not found.
     */
    public static boolean setFieldValue(Object object, String fieldName, Object fieldValue) {
        if (Objects.isNull(object) || Objects.isNull(fieldName)) {
            return false;
        }
        
        final Optional<Field> fieldOpt = getField(fieldName, object.getClass());
        final boolean fieldNotFound = fieldOpt.isEmpty();
        
        if (fieldNotFound){
            return false;
        }
        final Field field = fieldOpt.get();
        try {
            final boolean canAccess = field.canAccess(object);
            if (!canAccess) {
                field.setAccessible(true);
            }
            field.set(object, fieldValue);
            if (!canAccess) {
                field.setAccessible(false);
            }
        } catch (IllegalAccessException e) {
            return false;
        }
        return true;
    }
    
    /**
     * Find all public methods of certain class.
     * @param clazz Class that you want to find methods.
     * @return Empty list if input is null. Otherwise, return list of public methods
     */
    public static List<Method> getPublicMethodsOf(final Class<?> clazz) {
        if (clazz == null) {
            return CollectionHelper.emptyList();
        }
        return CollectionHelper.listOf(clazz.getDeclaredMethods()).stream()
            .filter(method -> Modifier.isPublic(method.getModifiers()))
            .collect(Collectors.toList());
    }
    
    /**
     * Get generic type for superclass.
     * @param clazz can be null. NOTE: This class must contains only one generic type.
     * @return Optional of generic type. Return empty if any exception caught.
     */
    public static Optional<Class<?>> genericTypeOf(Class<?> clazz) {
        return genericTypeOf(Objects.requireNonNull(clazz).getGenericSuperclass());
    }
    
    /**
     * Get generic type for superclass.
     * @param type can be null.
     * @return Optional of generic type. Return empty if any exception caught.
     */
    public static Optional<Class<?>> genericTypeOf(Type type) {
        try {
            Type genericType = Objects.requireNonNull((ParameterizedType) type).getActualTypeArguments()[0];
            return Optional.of((Class<?>) genericType);
        } catch (ClassCastException | NullPointerException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
    
    /**
     * Check whether a field is collection or not.
     * @param field can be null.
     * @return false if input is null or field is not collection. Otherwise, return true.
     */
    public static boolean isCollection(final Field field) {
        if (field == null) {
            return false;
        }
        return Collection.class.isAssignableFrom(field.getType());
    }
    
    /**
     * Check whether a field is primitive type or wrapper or not.
     * @param field can be null.
     * @return false if input is null or field is not primitive or wrapper. Otherwise, return true.
     */
    public static boolean isPrimitive(final Field field) {
        if (field == null) {
            return false;
        }
        Class<?> type = field.getType();
        return type.isPrimitive() || wrapperClasses.contains(field.getType());
    }
    
    /**
     * Get the type of the field. If field is collection, try to get the generic type.
     * @param field can be null.
     * @return empty if field is null. If field is collection, return generic of type.
     * Otherwise, return field's type.
     */
    public static Optional<Class<?>> getTypeOf(final Field field) {
        if (field == null) {
            return Optional.empty();
        }
        if (isCollection(field)) {
            return genericTypeOf(field.getGenericType());
        }
        return Optional.of(field.getType());
    }
}
