package ananta.api.models;

import ananta.api.helpers.StringHelper;
import ananta.api.helpers.TypeHelper;

public class TableJoin {
    private String name;
    private String column;
    
    private static final String SEPARATOR = ".";
    
    private TableJoin(final Builder builder) {
        name = builder.name;
        column = builder.column;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getName() {
        return name;
    }
    
    public String getColumn() {
        return column;
    }
    
    public static TableJoin of(String key) {
        TypeHelper.checkNull(key, "Key must not be null.");
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
    
    public static final class Builder {
        private String name;
        private String column;
        
        private Builder() {
        }
        
        public Builder name(final String val) {
            name = val;
            return this;
        }
        
        public Builder column(final String val) {
            column = val;
            return this;
        }
        
        public TableJoin build() {
            return new TableJoin(this);
        }
    }
}