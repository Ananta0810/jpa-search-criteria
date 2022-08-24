package ananta.api.helpers;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

@UtilityClass
public class StringHelper {
    private final static String EMPTY = "";
    
    @Nullable
    public static String afterOf(final String word, final String origin) {
        if (word == null || origin == null) {
            return origin;
        }
        int index = origin.indexOf(word);
        if (index < 0) {
            return "";
        }
        return origin.substring(index + 1);
    }
    
    @NotNull
    public static String beforeOf(final String word, final String origin) {
        if (word == null || origin == null) {
            return EMPTY;
        }
        int index = origin.indexOf(word);
        if (index < 0) {
            return "";
        }
        return origin.substring(0, index);
    }
    
    public static String format(String msg, Object... objs) {
        return MessageFormatter.arrayFormat(msg, objs).getMessage();
    }
}

