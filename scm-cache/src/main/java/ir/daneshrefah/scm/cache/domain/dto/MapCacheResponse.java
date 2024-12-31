package ir.daneshrefah.scm.cache.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MapCacheResponse {
    private String name;
    private int size;
    private long hits;
    private long heapCost;
    private long creationTime;
    private long lastUpdateTime;
    private long lastAccessTime;
}
