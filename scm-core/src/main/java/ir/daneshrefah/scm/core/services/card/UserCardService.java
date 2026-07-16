package ir.daneshrefah.scm.core.services.card;

import ir.daneshrefah.scm.common.model.customer.Card;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserCardService {

    private static final int MIN_CARD_LENGTH = 16;
    private static final int MAX_CARD_LENGTH = 19;
    private static final int MIN_MASK_PREFIX_LENGTH = 6;
    private static final int MIN_MASK_SUFFIX_LENGTH = 4;

    private final UserCardCacheService userCardCacheService;

    public List<Card> getUserCards(UserProfile userProfile) {
        return getUserCards(userProfile, currentUsername());
    }

    public List<Card> getUserCards(UserProfile userProfile, String fallbackUsername) {
        Objects.requireNonNull(userProfile, "userProfile is required");
        if (userProfile.getPersonId() != null) {
            return getUserCards(userProfile.getPersonId());
        }
        return userCardCacheService.getByUsername(resolveUsername(userProfile, fallbackUsername));
    }

    public List<Card> getUserCards(Integer personId) {
        return userCardCacheService.getByPersonId(personId);
    }

    public boolean hasCard(UserProfile userProfile, String cardNumber) {
        return hasCard(userProfile, currentUsername(), cardNumber);
    }

    public boolean hasCard(UserProfile userProfile, String fallbackUsername, String cardNumber) {
        String requestedCardNumber = normalize(cardNumber);
        if (!isValidCardPattern(requestedCardNumber)) {
            return false;
        }
        return getUserCards(userProfile, fallbackUsername).stream()
                .map(Card::getCardNumber)
                .map(this::normalize)
                .anyMatch(storedCardNumber -> cardMatches(storedCardNumber, requestedCardNumber));
    }

    public void evictUserCards(Integer personId) {
        userCardCacheService.evictByPersonId(personId);
    }


    public void evictUserCards(UserProfile userProfile) {
        Objects.requireNonNull(userProfile, "userProfile is required");
        if (userProfile.getPersonId() != null) {
            evictUserCards(userProfile.getPersonId());
            return;
        }
        userCardCacheService.evictByUsername(resolveUsername(userProfile, currentUsername()));
    }

    private boolean cardMatches(String storedCardNumber, String requestedCardNumber) {
        if (!isValidCardPattern(storedCardNumber) || storedCardNumber.length() != requestedCardNumber.length()) {
            return false;
        }
        if (!storedCardNumber.contains("*") && !requestedCardNumber.contains("*")) {
            return storedCardNumber.equals(requestedCardNumber);
        }
        for (int index = 0; index < storedCardNumber.length(); index++) {
            char stored = storedCardNumber.charAt(index);
            char requested = requestedCardNumber.charAt(index);
            if (stored != '*' && requested != '*' && stored != requested) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidCardPattern(String cardNumber) {
        if (StringUtils.isBlank(cardNumber)
                || cardNumber.length() < MIN_CARD_LENGTH
                || cardNumber.length() > MAX_CARD_LENGTH
                || !cardNumber.matches("[0-9*]+")) {
            return false;
        }
        int firstMask = cardNumber.indexOf('*');
        if (firstMask < 0) {
            return true;
        }
        int lastMask = cardNumber.lastIndexOf('*');
        return firstMask >= MIN_MASK_PREFIX_LENGTH
                && cardNumber.length() - lastMask - 1 >= MIN_MASK_SUFFIX_LENGTH
                && cardNumber.substring(firstMask, lastMask + 1).chars().allMatch(character -> character == '*');
    }

    private String normalize(String cardNumber) {
        return StringUtils.deleteWhitespace(cardNumber);
    }

    private String resolveUsername(UserProfile userProfile, String fallbackUsername) {
        String username = StringUtils.firstNonBlank(
                userProfile.getPersonUsername(),
                userProfile.getNickname(),
                fallbackUsername);
        if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("A stable user identifier is required to load cards");
        }
        return username.trim();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? null : authentication.getName();
    }
}
