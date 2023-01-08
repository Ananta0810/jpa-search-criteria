package ananta.api.helpers;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.HashMap;

public class TypeHelper {
    private static Gson GSON = new Gson();
    
    private TypeHelper() {}

    public static void configGson(final Gson gson) {
        GSON = gson;
    }
    
    public static void checkNull(final Object obj, final String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }
    
    public static <T> T convertFromMapToObject(final Class<T> objectClass, final HashMap<String, Object> objectValues) {
        final JsonElement jsonElement = GSON.toJsonTree(objectValues);
        return GSON.fromJson(jsonElement, objectClass);
    }
}
