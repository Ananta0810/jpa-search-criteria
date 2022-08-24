package ananta.api.helpers;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class CollectionHelper {
    public static <E, KEY, VALUE> Map<KEY, VALUE> mapOf(final List<E> items, final Function<E, KEY> keyProvider, final Function<E, VALUE> valueProvider) {
        return items.stream().collect(Collectors.toMap(keyProvider, valueProvider, (origin, duplicated) -> origin));
    }
    
    public static <K, V> Map<K, V> mapOf(final List<V> existingInventories, final Function<V, K> valueProvider) {
        return existingInventories.stream().collect(Collectors.toMap(valueProvider, item -> item, (origin, duplicated) -> origin));
    }
}
