package ir.daneshrefah.scm.cache.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemoveValueCacheRequest {
    private String name;
    private String key;
}
