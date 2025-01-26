package ir.daneshrefah.scm.cache.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MapValuesCacheResponse {
   private Object key;
   private Object value;
   private long cost;
   private long creationTime;
   private long expirationTime;
   private long hits;
   private long lastAccessTime;
   private long lastStoredTime;
   private long lastUpdateTime;
   private long version;
   private long ttl;
   private long maxIdle;
}
