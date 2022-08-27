package ananta.api.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.HashMap;

public class TypeHelper {
    private static final Gson GSON = new Gson();
    
    private TypeHelper() {}
    
    public static void checkNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }
    
    public static <T> T convertFromMapToObject(Class<T> objectClass, final HashMap<String, Object> objectValues) {
        JsonElement jsonElement = GSON.toJsonTree(objectValues);
        return GSON.fromJson(jsonElement, objectClass);
    }
}
