package ananta.api.helpers;

public class StringHelper {
    
    private StringHelper() {}
    
    private final static String EMPTY = "";
    
    public static String afterOf(final String word, final String origin) {
        if (word == null || origin == null) {
            return EMPTY;
        }
        int index = origin.indexOf(word);
        if (index < 0) {
            return EMPTY;
        }
        return origin.substring(index + 1);
    }
    
    public static String beforeOf(final String word, final String origin) {
        if (word == null || origin == null) {
            return EMPTY;
        }
        int index = origin.indexOf(word);
        if (index < 0) {
            return EMPTY;
        }
        return origin.substring(0, index);
    }
    
    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
    
    public static boolean isNotBlank(String value) {
        return !isBlank(value);
    }
}

