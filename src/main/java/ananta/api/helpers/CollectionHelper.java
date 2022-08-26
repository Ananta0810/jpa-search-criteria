package ananta.api.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionHelper {
    
    private CollectionHelper() {}
    
    public static <E, KEY, VALUE> Map<KEY, VALUE> mapOf(final Collection<E> items, final Function<E, KEY> keyProvider, final Function<E, VALUE> valueProvider) {
        return items.stream().collect(Collectors.toMap(keyProvider, valueProvider, (origin, duplicated) -> origin));
    }
    
    public static <K, V> Map<K, V> mapOf(final Collection<V> existingInventories, final Function<V, K> valueProvider) {
        return existingInventories.stream().collect(Collectors.toMap(valueProvider, item -> item, (origin, duplicated) -> origin));
    }
    
    public static <K, V> HashMap<K, V> emptyMap() {
        return new HashMap<>();
    }
    
    
    @SafeVarargs
    public static <T> List<T> listOf(T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }
    
    @SafeVarargs
    public static <T> Set<T> setOf(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }
    
    public static <T> Set<T> setOf(List<T> items) {
        return new HashSet<>(items);
    }
    
    public static <T> List<T> emptyList() {
        return new ArrayList<>();
    }
    
    public static <T> T getLastElementOf(List<T> list) {
        if (CollectionHelper.isEmpty(list)) {
            return null;
        }
        return list.get(list.size() - 1);
    }
    
    public static boolean isEmpty(Collection<?> items) {
        return items != null && !items.isEmpty();
    }
    
    public static boolean isNotEmpty(Collection<?> items) {
        return !isEmpty(items);
    }
}
