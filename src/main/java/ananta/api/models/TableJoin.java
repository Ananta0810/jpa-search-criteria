package ananta.api.models;

import ananta.api.helpers.StringHelper;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class TableJoin {
    private String name;
    private String column;
    
    private static final String SEPARATOR = ".";
    
    public static TableJoin of(String key) {
        Preconditions.checkNotNull(key, "Key must not be null.");
        if (!key.contains(SEPARATOR)) {
            return TableJoin.builder().column(key).build();
        }
        String table = StringHelper.beforeOf(SEPARATOR, key);
        String column = StringHelper.afterOf(SEPARATOR, key);
        return TableJoin.builder().name(table).column(column).build();
    }
    
    public String getNameOrElse(String otherName) {
        return StringHelper.isBlank(name) ? otherName : name;
    }
}