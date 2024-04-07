package ir.daneshrefah.scm.core.service.constant;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-06
 */
@RequiredArgsConstructor
@Component
public class ConstantCache {

    private static final String DEFAULT_CACHE_NAME = "constant_cache";
    private final CacheTemplate cacheTemplate;
}
