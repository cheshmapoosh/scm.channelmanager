package ir.daneshrefah.scm.cache.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RemoveMapValuesCacheRequest {
    private String mapName;
    private String key;
}
