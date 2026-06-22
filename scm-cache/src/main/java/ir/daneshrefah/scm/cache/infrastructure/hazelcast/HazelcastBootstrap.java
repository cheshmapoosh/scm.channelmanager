package ir.daneshrefah.scm.cache.infrastructure.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import ir.daneshrefah.scm.cache.domain.config.InstanceConfigEntity;
import ir.daneshrefah.scm.cache.observation.ScmCacheInitLogging;
import ir.daneshrefah.scm.cache.repository.InstanceCacheConfigRepository;
import ir.daneshrefah.scm.observation.logging.ScmLogMarkers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HazelcastBootstrap {
    private final InstanceCacheConfigRepository repository;
    private final HazelcastElementRegistry elementRegistry;

    public HazelcastInstance start(Config config) {
        logInit(
                "Hazelcast bootstrap started",
                "hazelcast.bootstrap.started",
                "unknown"
        );

        try {
            List<InstanceConfigEntity> definitions = repository.findAll();
            logInit(
                    "Hazelcast bootstrap config loaded",
                    "hazelcast.bootstrap.config.loaded",
                    "success"
            );

            HazelcastInitializationPlan plan = elementRegistry.register(config, definitions);
            logInit(
                    "Hazelcast bootstrap config registered",
                    "hazelcast.bootstrap.config.registered",
                    "success"
            );

            HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
            logInit(
                    "Hazelcast bootstrap member started",
                    "hazelcast.bootstrap.member.started",
                    "success"
            );

            elementRegistry.materialize(instance, plan);
            logInit(
                    "Hazelcast bootstrap objects materialized",
                    "hazelcast.bootstrap.objects.materialized",
                    "success"
            );

            logInit(
                    "Hazelcast bootstrap completed",
                    "hazelcast.bootstrap.completed",
                    "success"
            );

            return instance;
        } catch (RuntimeException exception) {
            logInitFailure(exception);
            throw exception;
        }
    }

    private void logInit(String message, String action, String outcome, Object... fields) {
        log.info(
                ScmLogMarkers.SCM_EVENT,
                message,
                ScmCacheInitLogging.initArguments(action, outcome, fields)
        );
    }

    private void logInitFailure(RuntimeException exception) {
        log.error(
                ScmLogMarkers.SCM_EVENT,
                "Hazelcast bootstrap failed",
                withThrowable(
                        ScmCacheInitLogging.initArguments(
                                "hazelcast.bootstrap.failed",
                                "failure"
                        ),
                        exception
                )
        );
    }

    private Object[] withThrowable(Object[] arguments, Throwable throwable) {
        Object[] result = Arrays.copyOf(arguments, arguments.length + 1);
        result[arguments.length] = throwable;
        return result;
    }

}
