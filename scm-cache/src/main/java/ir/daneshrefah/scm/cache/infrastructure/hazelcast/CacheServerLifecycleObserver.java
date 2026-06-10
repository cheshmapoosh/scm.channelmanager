package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import ir.daneshrefah.scm.cache.observation.ScmCacheInitLogging;
import ir.daneshrefah.scm.observation.logging.ScmLogMarkers;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheServerLifecycleObserver {

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        ScmCacheInitLogging.ensureInitCorrelationId();
        logInit("SCM cache init ready", "scm.cache.init.ready", "success");
    }

    @PreDestroy
    public void onShutdown() {
        ScmCacheInitLogging.ensureInitCorrelationId();
        logInit("SCM cache shutdown started", "scm.cache.init.shutdown.started", "unknown");
    }

    private void logInit(String message, String action, String outcome) {
        log.info(
                ScmLogMarkers.SCM_INIT,
                message,
                ScmCacheInitLogging.initArguments(action, outcome)
        );
    }
}
