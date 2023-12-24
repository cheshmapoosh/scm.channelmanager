package ir.daneshrefah.scm.uaa.security.userDetails;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
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

    /*private final CacheTemplate cacheTemplate;

    public UserCache(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }*/

    @Override
    public UserDetails getUserFromCache(String username) {
        return null;
    }

    @Override
    public void putUserInCache(UserDetails user) {

    }

    @Override
    public void removeUserFromCache(String username) {

    }

}
