package ir.daneshrefah.scm.core.card;

import ir.daneshrefah.scm.common.model.customer.Card;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.core.services.card.UserCardCacheService;
import ir.daneshrefah.scm.core.services.card.UserCardLoader;
import ir.daneshrefah.scm.core.services.card.UserCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(UserCardServiceTest.TestConfig.class)
class UserCardServiceTest {

    private static final String CARD_NUMBER = "6037991234561234";

    @Autowired
    private UserCardService userCardService;

    @Autowired
    private UserCardLoader userCardLoader;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void resetState() {
        reset(userCardLoader);
        cacheManager.getCache(UserCardCacheService.CACHE_NAME).clear();
    }

    @Test
    void loadsCardsOnceAndThenReadsFromCache() {
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of(card(CARD_NUMBER)));

        assertEquals(1, userCardService.getUserCards(1).size());
        assertEquals(1, userCardService.getUserCards(1).size());

        verify(userCardLoader, times(1)).loadByPersonId(1);
    }

    @Test
    void cachesEmptyListForUserWithoutCards() {
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of());

        assertTrue(userCardService.getUserCards(1).isEmpty());
        assertTrue(userCardService.getUserCards(1).isEmpty());

        verify(userCardLoader, times(1)).loadByPersonId(1);
    }

    @Test
    void matchesFullCardNumber() {
        UserProfile profile = profile(1);
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of(card(CARD_NUMBER)));

        assertTrue(userCardService.hasCard(profile, CARD_NUMBER));
    }

    @Test
    void removesWhitespaceBeforeMatching() {
        UserProfile profile = profile(1);
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of(card("6037 9912 3456 1234")));

        assertTrue(userCardService.hasCard(profile, "6037 9912 3456 1234"));
    }

    @Test
    void matchesValidMaskedCardNumber() {
        UserProfile profile = profile(1);
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of(card(CARD_NUMBER)));

        assertTrue(userCardService.hasCard(profile, "603799******1234"));
    }

    @Test
    void rejectsWrongAndInvalidLengthMaskedCardNumbers() {
        UserProfile profile = profile(1);
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of(card(CARD_NUMBER)));

        assertFalse(userCardService.hasCard(profile, "6037999999991234"));
        assertFalse(userCardService.hasCard(profile, "603799***1234"));
    }

    @Test
    void rejectsNullAndBlankWithoutLoadingCards() {
        UserProfile profile = profile(1);

        assertFalse(userCardService.hasCard(profile, null));
        assertFalse(userCardService.hasCard(profile, "  \t"));

        verify(userCardLoader, times(0)).loadByPersonId(1);
    }

    @Test
    void keepsTwoUsersCacheEntriesSeparate() {
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of(card(CARD_NUMBER)));
        when(userCardLoader.loadByPersonId(2)).thenReturn(List.of(card("5894631234567890")));

        assertEquals(CARD_NUMBER, userCardService.getUserCards(1).getFirst().getCardNumber());
        assertEquals("5894631234567890", userCardService.getUserCards(2).getFirst().getCardNumber());
        userCardService.getUserCards(1);
        userCardService.getUserCards(2);

        verify(userCardLoader, times(1)).loadByPersonId(1);
        verify(userCardLoader, times(1)).loadByPersonId(2);
    }

    @Test
    void evictsOneUserWithoutEvictingAnotherAndReloadsEvictedUser() {
        when(userCardLoader.loadByPersonId(1)).thenReturn(List.of(card(CARD_NUMBER)));
        when(userCardLoader.loadByPersonId(2)).thenReturn(List.of(card("5894631234567890")));
        userCardService.getUserCards(1);
        userCardService.getUserCards(2);

        userCardService.evictUserCards(1);
        userCardService.getUserCards(1);
        userCardService.getUserCards(2);

        verify(userCardLoader, times(2)).loadByPersonId(1);
        verify(userCardLoader, times(1)).loadByPersonId(2);
    }

    @Test
    void personIdAndNumericUsernameCannotCollide() {
        UserProfile idProfile = profile(7);
        UserProfile usernameProfile = new UserProfile("nickname-7", "7", null);
        when(userCardLoader.loadByPersonId(7)).thenReturn(List.of(card(CARD_NUMBER)));
        when(userCardLoader.loadByUsername("7")).thenReturn(List.of(card("5894631234567890")));

        assertEquals(CARD_NUMBER, userCardService.getUserCards(idProfile).getFirst().getCardNumber());
        assertEquals("5894631234567890", userCardService.getUserCards(usernameProfile).getFirst().getCardNumber());

        verify(userCardLoader).loadByPersonId(7);
        verify(userCardLoader).loadByUsername("7");
    }

    private UserProfile profile(Integer personId) {
        return new UserProfile("nickname", "person-username", personId);
    }

    private Card card(String cardNumber) {
        Card card = new Card();
        card.setCardNumber(cardNumber);
        return card;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(UserCardCacheService.CACHE_NAME);
        }

        @Bean
        UserCardLoader userCardLoader() {
            return mock(UserCardLoader.class);
        }

        @Bean
        UserCardCacheService userCardCacheService(UserCardLoader userCardLoader) {
            return new UserCardCacheService(userCardLoader);
        }

        @Bean
        UserCardService userCardService(UserCardCacheService userCardCacheService) {
            return new UserCardService(userCardCacheService);
        }
    }
}
