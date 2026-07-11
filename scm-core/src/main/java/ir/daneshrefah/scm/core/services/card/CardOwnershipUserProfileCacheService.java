package ir.daneshrefah.scm.core.services.card;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.customer.UserProfileThreadLocal;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class CardOwnershipUserProfileCacheService {

    private static final String CACHE_KEY_PREFIX = "USER_PROFILE";

    private final CacheManager cacheManager;
    private final CardService cardService;
    private final PersonProfileLoader personProfileLoader;

    public CardOwnershipUserProfileCacheResult getValidatedUserProfile(String cacheName,
                                                                       boolean cacheEnabled,
                                                                       Authentication authentication,
                                                                       String cardNumber,
                                                                       boolean failIfCardNotFound,
                                                                       boolean failIfCardNotOwnedByUser) {
        UserProfile initialProfile = authentication == null ? null : authentication.getProfile();
        if (initialProfile == null) {
            throw new AccessDeniedException("CURRENT_USER_NOT_RESOLVED", ErrorCodes.ERROR_CODE_ACCESS_DENIED, "CURRENT_USER_NOT_RESOLVED");
        }
        UserProfile threadLocalProfile = UserProfileThreadLocal.get().orElse(null);
        if (threadLocalProfile != null) {
            if (sameUser(threadLocalProfile, initialProfile, authentication)) {
                return validateProfile(threadLocalProfile, cardNumber, failIfCardNotOwnedByUser);
            }
            UserProfileThreadLocal.clear();
        }

        String cacheKey = cacheKey(initialProfile, authentication);
        Cache cache = cacheEnabled ? cache(cacheName) : null;
        UserProfile cached = cache == null ? null : cache.get(cacheKey, UserProfile.class);
        if (cached != null) {
            UserProfileThreadLocal.set(cached);
            return validateProfile(cached, cardNumber, failIfCardNotOwnedByUser);
        }

        UserProfile profile = personProfileLoader.preparePersonProfileMemberships(authentication);
        if (profile == null) {
            throw new AccessDeniedException("CURRENT_USER_NOT_RESOLVED", ErrorCodes.ERROR_CODE_ACCESS_DENIED, "CURRENT_USER_NOT_RESOLVED");
        }
        loadCardsIfRequired(profile, authentication);
        if (cache != null) {
            cache.put(cacheKey, profile);
            String resolvedCacheKey = cacheKey(profile, authentication);
            if (!StringUtils.equals(cacheKey, resolvedCacheKey)) {
                cache.put(resolvedCacheKey, profile);
            }
        }
        UserProfileThreadLocal.set(profile);
        return validateProfile(profile, cardNumber, failIfCardNotOwnedByUser);
    }

    private CardOwnershipUserProfileCacheResult validateProfile(UserProfile profile,
                                                               String cardNumber,
                                                               boolean failIfCardNotOwnedByUser) {
        boolean owned = profile.hasCard(cardNumber);
        if (!owned) {
            if (failIfCardNotOwnedByUser) {
                throw new AccessDeniedException(
                        "CARD_DOES_NOT_BELONG_TO_CURRENT_USER",
                        ErrorCodes.ERROR_CODE_ACCESS_DENIED,
                        "CARD_DOES_NOT_BELONG_TO_CURRENT_USER");
            }
            return new CardOwnershipUserProfileCacheResult(profile, false);
        }
        return new CardOwnershipUserProfileCacheResult(profile, true);
    }

    private void loadCardsIfRequired(UserProfile profile, Authentication authentication) {
        if (profile.isCardsLoaded()) {
            return;
        }
        profile.loadCards(cardService.findUserCards(profile, authentication.getName()));
    }

    private boolean sameUser(UserProfile threadLocalProfile, UserProfile currentProfile, Authentication authentication) {
        String threadLocalOwnerKey = ownerKey(threadLocalProfile, authentication);
        String currentOwnerKey = ownerKey(currentProfile, authentication);
        return StringUtils.isNotBlank(threadLocalOwnerKey) && StringUtils.equals(threadLocalOwnerKey, currentOwnerKey);
    }

    private String cacheKey(UserProfile profile, Authentication authentication) {
        return CACHE_KEY_PREFIX + "::" + sha256(ownerKey(profile, authentication));
    }

    private String ownerKey(UserProfile profile, Authentication authentication) {
        return profile.getPersonId() != null
                ? String.valueOf(profile.getPersonId())
                : StringUtils.defaultIfBlank(profile.getPersonUsername(), authentication.getName());
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Could not hash card ownership cache key", e);
        }
    }

    private Cache cache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + cacheName);
        }
        return cache;
    }
}
