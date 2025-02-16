package ir.daneshrefah.scm.uaa.security.userDetails;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Component
public class UserCache implements org.springframework.security.core.userdetails.UserCache {

    public static final String DOUBLE_COLON = "::";
    private final CacheTemplate cacheTemplate;
    private static final String USER_CACHE_NAME = "user_cache";

    public UserCache(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    /**
     * Get a user from the cache based on username
     * <p>
     * Note: The username corresponds to the 'nickName' field in the USER_CHANNEL_AUTHENTICATION table.
     * </p>
     *
     * @param username     The nickname of the user in the USER_CHANNEL_AUTHENTICATION table.
     */
    @Override
    public UserDetails getUserFromCache(String username) {
        return (UserDetails) cacheTemplate.getFromCache(USER_CACHE_NAME, username);
    }

    @Override
    public void putUserInCache(UserDetails user) {
        String userKey = user.getUsername() + StringUtils.DOUBLE_COLON +
                ((TerminalUserDetails) user).getUser().getTerminalCode();
        cacheTemplate.putInCache(USER_CACHE_NAME, userKey, user);
    }

    /**
     * Removes a user from the cache based on their username and terminal code.
     * <p>
     * Note: The username corresponds to the 'nickName' field in the USER_CHANNEL_AUTHENTICATION table.
     * </p>
     *
     * @param username     The nickname of the user in the USER_CHANNEL_AUTHENTICATION table.
     * @param terminalCode The terminal code associated with the user.
     */
    public void removeUserFromCache(String username, String terminalCode) {
        String key = username + DOUBLE_COLON + terminalCode.toUpperCase();
        removeUserFromCache(key);
    }

    @Override
    public void removeUserFromCache(String username) {
        cacheTemplate.removeFromCache(USER_CACHE_NAME,username);
    }
}
