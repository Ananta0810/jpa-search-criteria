package ananta.api.helpers;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class StringHelper {
    private final static String EMPTY = "";
    
    public static @NotNull String afterOf(@Nullable final String word, @Nullable final String origin) {
        if (word == null || origin == null) {
            return EMPTY;
        }
        int index = origin.indexOf(word);
        if (index < 0) {
            return EMPTY;
        }
        return origin.substring(index + 1);
    }
    
    public static @NotNull String beforeOf(@Nullable final String word, @Nullable final String origin) {
        if (word == null || origin == null) {
            return EMPTY;
        }
        int index = origin.indexOf(word);
        if (index < 0) {
            return EMPTY;
        }
        return origin.substring(0, index);
    }
    
    public static boolean isBlank(@Nullable String value) {
        return value == null || value.isBlank();
    }
    
    public static boolean isNotBlank(@Nullable String value) {
        return !isBlank(value);
    }
}

