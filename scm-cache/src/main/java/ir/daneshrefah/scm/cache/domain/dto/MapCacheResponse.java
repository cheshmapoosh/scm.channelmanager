package ir.daneshrefah.scm.cache.domain.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@Builder
public class MapCacheResponse {
    private String name;
    private int size;
    private long hits;
    private long heapCost;
    private Date creationTime;
    private Date lastUpdateTime;
    private Date lastAccessTime;
}
