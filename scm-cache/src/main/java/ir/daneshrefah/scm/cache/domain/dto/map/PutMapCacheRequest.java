package ir.daneshrefah.scm.cache.domain.dto.map;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class PutMapCacheRequest {
    private final static TimeUnit TIME_TO_LIVA_UNIT = TimeUnit.SECONDS;
    private final static TimeUnit TIME_MAX_IDLE_UNIT = TimeUnit.SECONDS;
    private String name;
    private String key;
    private Object value;
    private Integer timeToLive;
    private TimeUnit timeToLiveUnit = TIME_TO_LIVA_UNIT;
    private Integer maxIdle;
    private TimeUnit maxIdleUnit = TIME_MAX_IDLE_UNIT;
}
