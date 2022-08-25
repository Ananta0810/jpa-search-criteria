package ananta.api.helpers;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class CollectionHelper {
    public static <E, KEY, VALUE> Map<KEY, VALUE> mapOf(final @NotNull Collection<E> items, final Function<E, KEY> keyProvider, final Function<E, VALUE> valueProvider) {
        return items.stream().collect(Collectors.toMap(keyProvider, valueProvider, (origin, duplicated) -> origin));
    }
    
    public static <K, V> Map<K, V> mapOf(final @NotNull Collection<V> existingInventories, final Function<V, K> valueProvider) {
        return existingInventories.stream().collect(Collectors.toMap(valueProvider, item -> item, (origin, duplicated) -> origin));
    }
    
    public static <T> @Nullable T getLastElementOf(List<T> list) {
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
