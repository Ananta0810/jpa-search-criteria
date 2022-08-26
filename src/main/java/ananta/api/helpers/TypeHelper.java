package ananta.api.helpers;

public class TypeHelper {
    
    private TypeHelper() {}
    
    public static void checkNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }
}
