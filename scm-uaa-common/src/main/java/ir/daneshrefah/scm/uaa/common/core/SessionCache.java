package ir.daneshrefah.scm.uaa.common.core;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-25
 */
public class SessionCache {

    private final CacheTemplate cacheTemplate;

    public SessionCache(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    public UserAuthentication getSessionFromCache(String sessionKey) {
        return null;
    }

    public void putSessionInCache(UserAuthentication user) {

    }

    public void removeSessionFromCache(String sessionKey) {

    }
}
