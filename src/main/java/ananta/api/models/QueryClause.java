package ananta.api.models;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class QueryClause {
    private final boolean isAndClause;
    private final WhereClause clause;
}
