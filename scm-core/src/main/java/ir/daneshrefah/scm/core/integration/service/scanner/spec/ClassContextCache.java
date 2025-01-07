package ir.daneshrefah.scm.core.integration.service.scanner.spec;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ClassContextCache {

    private static final ClassContextCache INSTANCE = new ClassContextCache();

    private static final Map<Repository, Map<String, Object>> CONTAINER_CACHE = new ConcurrentHashMap<>(Repository.values().length);

    static {
        initContainer();
    }

    private ClassContextCache() {
    }

    private static void initContainer() {
        Arrays.stream(Repository.values())
                .forEach(container -> {
                    Map<String, Object> repository = new ConcurrentHashMap<>();
                    CONTAINER_CACHE.put(container, repository);
                    log.info(">>> [CONTEXT-CACHE] :: [{}] Cache Repository Successfully initialized", container.name());
                });
        log.info(">>> [CONTEXT-CACHE] :: Cache Context container Successfully Initialized {} Repositories.", CONTAINER_CACHE.size());
    }

    public static ClassContextCache getInstance() {
        return INSTANCE;
    }

    public Map<String, Object> getRepository(Repository repository) {
        return CONTAINER_CACHE.get(repository);
    }

    public void put(Repository repository, String key, Object value) {
        getRepository(repository).put(key, value);
    }

    public Optional<Object> get(Repository repository, String key) {
        if (getRepository(repository).containsKey(key)) {
            return Optional.of(getRepository(repository).get(key));
        }
        return Optional.empty();
    }

    public <T> Optional<T> get(Repository repository, String key, Class<T> resultType) {
        if (getRepository(repository).containsKey(key)) {
            return Optional.of(resultType.cast(getRepository(repository).get(key)));
        }
        return Optional.empty();
    }

    public void remove(Repository repository, String key) {
        getRepository(repository).remove(key);
    }

    public enum Repository {
        CLASSES,
        JAVA_SERVICE_METADATA
    }


}
