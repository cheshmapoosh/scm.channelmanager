package ir.daneshrefah.scm.core.card;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.services.card.CardOwnershipUserProfileCacheResult;
import ir.daneshrefah.scm.core.services.card.CardOwnershipUserProfileCacheService;
import ir.daneshrefah.scm.core.services.card.UserCardService;
import ir.daneshrefah.scm.common.model.customer.UserProfileThreadLocal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardOwnershipUserProfileCacheServiceTest {

    private static final String CACHE_NAME = "USER_PROFILE_CARD_OWNERSHIP_CACHE";
    private static final String CARD_NUMBER = "5894631240207563";

    private final UserCardService userCardService = mock(UserCardService.class);
    private final PersonProfileLoader profileLoader = mock(PersonProfileLoader.class);
    private final CardOwnershipUserProfileCacheService service = new CardOwnershipUserProfileCacheService(
            new ConcurrentMapCacheManager(),
            userCardService,
            profileLoader);

    @BeforeEach
    void clearThreadLocal() {
        UserProfileThreadLocal.clear();
    }

    @Test
    void cacheMissLoadsProfileAndStoresItWhenCardBelongs() {
        UserProfile profile = profile(10);
        Authentication authentication = authentication(profile);
        when(profileLoader.preparePersonProfileMemberships(authentication)).thenReturn(profile);
        when(userCardService.hasCard(profile, "user1", CARD_NUMBER)).thenReturn(true);

        CardOwnershipUserProfileCacheResult result = service.getValidatedUserProfile(
                CACHE_NAME, true, authentication, CARD_NUMBER, true, true);

        assertTrue(result.validated());
        assertSame(profile, result.userProfile());
        verify(profileLoader).preparePersonProfileMemberships(authentication);
        verify(userCardService).hasCard(profile, "user1", CARD_NUMBER);
    }

    @Test
    void cacheHitDoesNotLoadProfileAgain() {
        UserProfile profile = profile(10);
        Authentication authentication = authentication(profile);
        when(profileLoader.preparePersonProfileMemberships(authentication)).thenReturn(profile);
        when(userCardService.hasCard(profile, "user1", CARD_NUMBER)).thenReturn(true);

        service.getValidatedUserProfile(CACHE_NAME, true, authentication, CARD_NUMBER, true, true);
        UserProfileThreadLocal.clear();
        org.mockito.Mockito.reset(profileLoader);
        org.mockito.Mockito.reset(userCardService);
        when(userCardService.hasCard(profile, "user1", CARD_NUMBER)).thenReturn(true);

        service.getValidatedUserProfile(CACHE_NAME, true, authentication, CARD_NUMBER, true, true);

        verify(profileLoader, never()).preparePersonProfileMemberships(authentication);
        verify(userCardService).hasCard(profile, "user1", CARD_NUMBER);
    }

    @Test
    void threadLocalHitDoesNotReadCacheOrDatabase() {
        UserProfile profile = profile(10);
        UserProfileThreadLocal.set(profile);
        when(userCardService.hasCard(profile, "user1", CARD_NUMBER)).thenReturn(true);

        CardOwnershipUserProfileCacheResult result = service.getValidatedUserProfile(
                CACHE_NAME, true, authentication(profile), CARD_NUMBER, true, true);

        assertTrue(result.validated());
        assertSame(profile, result.userProfile());
        verify(profileLoader, never()).preparePersonProfileMemberships(org.mockito.ArgumentMatchers.any());
        verify(userCardService).hasCard(profile, "user1", CARD_NUMBER);
    }

    @Test
    void cardNotInUserProfileCardsThrows() {
        UserProfile profile = profile(10);
        Authentication authentication = authentication(profile);
        when(profileLoader.preparePersonProfileMemberships(authentication)).thenReturn(profile);
        when(userCardService.hasCard(profile, "user1", CARD_NUMBER)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.getValidatedUserProfile(CACHE_NAME, true, authentication, CARD_NUMBER, true, true));
    }

    @Test
    void maskedCardInUserProfileMatchesRequestedCard() {
        UserProfile profile = profile(10);
        Authentication authentication = authentication(profile);
        when(profileLoader.preparePersonProfileMemberships(authentication)).thenReturn(profile);
        when(userCardService.hasCard(profile, "user1", CARD_NUMBER)).thenReturn(true);

        CardOwnershipUserProfileCacheResult result = service.getValidatedUserProfile(
                CACHE_NAME, true, authentication, CARD_NUMBER, true, true);

        assertTrue(result.validated());
        assertSame(profile, result.userProfile());
    }

    private UserProfile profile(Integer userId) {
        return new UserProfile("user1", "person1", userId);
    }

    private Authentication authentication(UserProfile profile) {
        return new TestAuthentication(profile);
    }

    private static class TestAuthentication implements Authentication {
        private final UserProfile profile;

        TestAuthentication(UserProfile profile) {
            this.profile = profile;
        }

        @Override
        public String getName() {
            return "user1";
        }

        @Override
        public UserProfile getProfile() {
            return profile;
        }

        @Override
        public String getTerminalCode() {
            return "MB";
        }

        @Override
        public boolean isAnonymous() {
            return false;
        }

        @Override
        public boolean isDelegated() {
            return false;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public boolean isFullyAuthenticated() {
            return true;
        }

        @Override
        public boolean hasAuthority(String authorityName) {
            return false;
        }

        @Override
        public boolean hasError() {
            return false;
        }

        @Override
        public AuthenticationMethod getAuthenticationMethod() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return "user1";
        }

        @Override
        public Boolean getIsTransactionAuthenticated() {
            return false;
        }

        @Override
        public void authenticateTransaction(boolean isTransactionAuthenticated) {
        }
    }
}
