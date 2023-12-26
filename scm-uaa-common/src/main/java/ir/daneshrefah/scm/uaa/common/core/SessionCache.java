package ir.daneshrefah.scm.uaa.common.core;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InvalidClassException;

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
    private final CacheTemplate cacheTemplate;

    public SessionCache(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public UserAuthentication getSessionFromCache(String sessionKey) {
        UserAuthentication result = null;
        try {
            result = (UserAuthentication) cacheTemplate.getFromCache(DEFAULT_CACHE_NAME, sessionKey);
        } catch (Exception e) {
            logger.error("error session deserialize." , e);
            removeSessionFromCache(sessionKey);
        }
        return result;
    }

    public void putSessionInCache(String sessionKey, UserAuthentication user) {
        cacheTemplate.putInCache(DEFAULT_CACHE_NAME, sessionKey, user);
    }

    public void removeSessionFromCache(String sessionKey) {
        cacheTemplate.removeFromCache(DEFAULT_CACHE_NAME, sessionKey);
    }
}
