package ir.daneshrefah.scm.cache.domain.dto.list;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ListValuesCacheResponse {
    private Object value;
}
