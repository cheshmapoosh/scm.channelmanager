package ir.daneshrefah.scm.web.observation.cache;

import ir.daneshrefah.scm.common.event.ScmSafeEventAttributes;
import ir.daneshrefah.scm.common.event.cache.ScmCacheEvent;
import ir.daneshrefah.scm.web.observation.ScmWebObservationEvent;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ScmWebCacheObservationMapper {
    private static final String CACHE_MISS = "cache.miss";
    private static final String CACHE_ERROR = "cache.error";
    private static final String CACHE_LOCK_FAILED = "cache.lock.failed";
    private static final String CACHE_RATE_LIMIT_REJECTED = "cache.rate_limit.rejected";

    public ScmWebObservationEvent map(ScmCacheEvent event) {
        String action = event == null ? "cache.event" : event.eventType();
        Map<String, Object> attributes = attributes(event);
        return new ScmWebObservationEvent(
                "cache",
                action,
                outcome(action),
                "SCM cache event",
                attributes,
                attributes
        );
    }

    private Map<String, Object> attributes(ScmCacheEvent event) {
        Map<String, Object> attributes = event == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(ScmSafeEventAttributes.mutableCopyOf(event.attributes()));
        if (event != null) {
            attributes.put("scm.event.type", event.eventType());
            attributes.put("scm.event.source", event.metadata().source());
            attributes.put("scm.event.occurred_at", event.occurredAt().toString());
        }
        return attributes;
    }

    private String outcome(String action) {
        if (CACHE_MISS.equals(action)) {
            return "miss";
        }
        if (CACHE_ERROR.equals(action) || CACHE_LOCK_FAILED.equals(action) || CACHE_RATE_LIMIT_REJECTED.equals(action)) {
            return "failure";
        }
        return "success";
    }
}
