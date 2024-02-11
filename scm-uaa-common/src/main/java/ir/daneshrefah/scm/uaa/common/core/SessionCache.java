package ir.daneshrefah.scm.uaa.common.core;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.date.DateUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    public UserAuthentication getSessionFromCache(String username, String terminalCode) {
        String sessionKey = username + StringUtils.DOUBLE_COLON + terminalCode;
        return getSessionFromCache(sessionKey);
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

    public void putSessionInCache(UserAuthentication user) {
        //TODO username
        String sessionKey = user.getName() + StringUtils.DOUBLE_COLON + user.getPrincipal().getTerminalCode();
        putSessionInCache(sessionKey, user);
    }

    public void putSessionInCache(String sessionKey, UserAuthentication user) {
        long timeToLiveMinutes = DateUtils.InstantTools.calculateMinutesBetween(user.getDetails().getIssuedAt(), user.getDetails().getExpiresAt());
        cacheTemplate.putInCache(DEFAULT_CACHE_NAME, sessionKey, user, timeToLiveMinutes);
    }

    public void removeSessionFromCache(String sessionKey) {
        cacheTemplate.removeFromCache(DEFAULT_CACHE_NAME, sessionKey);
    }
}
