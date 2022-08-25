package ananta.api.helpers;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TypeHelper {
    public static void checkNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }
}
