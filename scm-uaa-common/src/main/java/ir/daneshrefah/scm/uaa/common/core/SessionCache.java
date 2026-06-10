package ir.daneshrefah.scm.uaa.common.core;

import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-25
 */
public class SessionCache {

    protected final Log logger = LogFactory.getLog(getClass());

    private static final String DEFAULT_CACHE_NAME = "session_cache";
    private final CacheManager cacheManager;
    private final String cacheName;

    public SessionCache(CacheManager cacheManager) {
        this(cacheManager, DEFAULT_CACHE_NAME);
    }

    public SessionCache(CacheManager cacheManager, String cacheName) {
        this.cacheManager = cacheManager;
        this.cacheName = StringUtils.isEmpty(cacheName) ? DEFAULT_CACHE_NAME : cacheName.trim();
    }

    public UserAuthentication getSessionFromCache(String username, String terminalCode) {
        String sessionKey = username + StringUtils.DOUBLE_COLON + terminalCode;
        return getSessionFromCache(sessionKey);
    }

    public UserAuthentication getSessionFromCache(String sessionKey) {
        UserAuthentication result = null;
        try {
            Cache.ValueWrapper valueWrapper = sessionCache().get(sessionKey);
            result = valueWrapper == null ? null : (UserAuthentication) valueWrapper.get();
        } catch (Exception e) {
            logger.error("error session deserialize." , e);
            removeSessionFromCache(sessionKey);
        }
        return result;
    }

    public void putSessionInCache(UserAuthentication user) {
        //TODO username
        String sessionKey = user.getName() + StringUtils.DOUBLE_COLON + user.getPrincipal().getTerminalCode();
        putSessionInCache(sessionKey, user);
    }

    public void putSessionInCache(String sessionKey, UserAuthentication user) {
        long timeToLiveMinutes = DateUtils.InstantTools.calculateMinutesBetween(user.getDetails().getIssuedAt(), user.getDetails().getExpiresAt());
        Cache cache = sessionCache();
        Duration ttl = Duration.ofMinutes(timeToLiveMinutes);
        if (putWithTtl(cache, sessionKey, user, ttl)) {
            return;
        }
        cache.put(sessionKey, user);
    }

    public void removeSessionFromCache(String sessionKey) {
        sessionCache().evict(sessionKey);
    }

    private Cache sessionCache() {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + cacheName);
        }
        return cache;
    }

    private boolean putWithTtl(Cache cache, String sessionKey, UserAuthentication user, Duration ttl) {
        try {
            Method putWithTtl = cache.getClass().getMethod("put", Object.class, Object.class, Duration.class);
            putWithTtl.invoke(cache, sessionKey, user, ttl);
            return true;
        } catch (NoSuchMethodException exception) {
            return false;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Could not write session cache entry with ttl.", exception);
        }
    }
}
