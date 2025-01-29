package ir.daneshrefah.scm.cache.domain.dto.list;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PutListCacheRequest {
    private String name;
    private Object value;
}
