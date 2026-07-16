package ir.daneshrefah.scm.core.services.card;

import ir.daneshrefah.scm.common.model.customer.Card;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCardCacheService {

    public static final String CACHE_NAME = "userCards";

    private final UserCardLoader userCardLoader;

    @Cacheable(cacheNames = CACHE_NAME, key = "'personId:' + #personId", unless = "#result == null")
    public List<Card> getByPersonId(Integer personId) {
        if (personId == null) {
            throw new IllegalArgumentException("personId is required");
        }
        return userCardLoader.loadByPersonId(personId);
    }

    @Cacheable(cacheNames = CACHE_NAME, key = "'username:' + #username", unless = "#result == null")
    public List<Card> getByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        return userCardLoader.loadByUsername(username);
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "'personId:' + #personId", condition = "#personId != null")
    public void evictByPersonId(Integer personId) {
        // Eviction is performed by Spring's cache interceptor.
    }

    @CacheEvict(cacheNames = CACHE_NAME, key = "'username:' + #username", condition = "#username != null && !#username.isBlank()")
    public void evictByUsername(String username) {
        // Eviction is performed by Spring's cache interceptor.
    }
}
