package ir.daneshrefah.scm.cache.client.security;

import ir.daneshrefah.scm.cache.client.config.properties.CacheClientProperties;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;

class UserDetailsKeyResolvingCacheTest {

    @Test
    void keepsSpringCacheBasedUserCacheForStandardUserDetails() {
        UserCache userCache = springUserCache(new CacheClientProperties.UserCache());
        UserDetails user = User.withUsername("reza").password("n/a").authorities(List.of()).build();

        userCache.putUserInCache(user);

        assertSame(user, userCache.getUserFromCache("reza"));
    }

    @Test
    void letsSpringCacheBasedUserCacheStoreTerminalUserDetailsByUsernameAndTerminalCode() {
        UserCache userCache = springUserCache(new CacheClientProperties.UserCache());
        TerminalAwareUserDetails user = new TerminalAwareUserDetails("reza", "MB");

        userCache.putUserInCache(user);

        assertSame(user, userCache.getUserFromCache("reza::MB"));
    }

    @Test
    void letsSpringCacheBasedUserCacheUseExplicitKeyExpression() {
        CacheClientProperties.UserCache properties = new CacheClientProperties.UserCache();
        properties.setKeyExpression("#user.username + '::custom'");
        UserCache userCache = springUserCache(properties);
        UserDetails user = User.withUsername("reza").password("n/a").authorities(List.of()).build();

        userCache.putUserInCache(user);

        assertSame(user, userCache.getUserFromCache("reza::custom"));
    }

    private UserCache springUserCache(CacheClientProperties.UserCache properties) {
        Cache cache = new ConcurrentMapCacheManager("user_cache").getCache("user_cache");
        return new SpringCacheBasedUserCache(new UserDetailsKeyResolvingCache(cache, properties));
    }

    private record TerminalAwareUserDetails(String username, String terminalCode) implements UserDetails {

        public DomainUser getUser() {
            return new DomainUser(terminalCode);
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public Collection<org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return List.of();
        }

        @Override
        public String getPassword() {
            return "n/a";
        }
    }

    private record DomainUser(String terminalCode) {

        public String getTerminalCode() {
            return terminalCode;
        }
    }
}
