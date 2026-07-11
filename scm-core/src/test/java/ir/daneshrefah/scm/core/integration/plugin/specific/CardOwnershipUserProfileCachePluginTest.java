package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.core.services.card.CardOwnershipUserProfileCacheResult;
import ir.daneshrefah.scm.core.services.card.CardOwnershipUserProfileCacheService;
import ir.daneshrefah.scm.core.integration.plugin.support.PluginMessageValueReader;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.support.DefaultExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyBoolean;

class CardOwnershipUserProfileCachePluginTest {

    private static final String CARD_NUMBER = "5894631240207563";
    private final CardOwnershipUserProfileCacheService service = mock(CardOwnershipUserProfileCacheService.class);
    private final CardOwnershipUserProfileCachePlugin plugin = new CardOwnershipUserProfileCachePlugin(
            new ObjectMapper(),
            new PluginMessageValueReader(new ObjectMapper()),
            service);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void readsCardNumberFromNestedBodyPathAndWritesHeaders() {
        UserProfile profile = authenticate(10);
        when(service.getValidatedUserProfile(eq("USER_PROFILE_CARD_OWNERSHIP_CACHE"), eq(true), any(Authentication.class),
                eq(CARD_NUMBER), eq(true), eq(true)))
                .thenReturn(new CardOwnershipUserProfileCacheResult(profile, true));

        Exchange exchange = exchange(Map.of("card", Map.of("sourceCardNumber", CARD_NUMBER)));

        plugin.handle(exchange, pluginDetail(config(true)));

        assertEquals(CARD_NUMBER, exchange.getMessage().getHeader("sourceCardNumber"));
        assertEquals(true, exchange.getMessage().getHeader("cardOwnershipValidated"));
        assertSame(profile, exchange.getMessage().getHeader("userProfile"));
    }

    @Test
    void failsWhenCardNumberIsMissingAndConfiguredToFail() {
        authenticate(10);
        Exchange exchange = exchange(Map.of("card", Map.of("companyCode", "di")));

        assertThrows(MissingRequiredInputException.class, () -> plugin.handle(exchange, pluginDetail(config(true))));
    }

    @Test
    void disabledPluginDoesNothing() {
        Exchange exchange = exchange(Map.of("card", Map.of("sourceCardNumber", CARD_NUMBER)));

        plugin.handle(exchange, pluginDetail(Map.of("enabled", false)));

        verify(service, never()).getValidatedUserProfile(any(), anyBoolean(), any(), any(), anyBoolean(), anyBoolean());
    }

    private Exchange exchange(Object body) {
        Exchange exchange = new DefaultExchange(new DefaultCamelContext());
        exchange.getMessage().setBody(body);
        return exchange;
    }

    private PluginDetail pluginDetail(Map<String, ?> config) {
        PluginDetail detail = new PluginDetail();
        detail.setName("cardOwnershipUserProfileCachePlugin");
        detail.setConfig(config);
        return detail;
    }

    private Map<String, ?> config(boolean failIfCardNotFound) {
        return Map.ofEntries(
                entry("enabled", true),
                entry("failIfCardNotFound", failIfCardNotFound),
                entry("failIfCardNotOwnedByUser", true),
                entry("cacheEnabled", true),
                entry("cacheName", "USER_PROFILE_CARD_OWNERSHIP_CACHE"),
                entry("cardNumberSources", List.of("body:card.sourceCardNumber")),
                entry("cardNumberHeaderTarget", "sourceCardNumber"),
                entry("putUserProfileInHeader", true),
                entry("userProfileHeaderName", "userProfile"),
                entry("putOwnershipResultInHeader", true),
                entry("ownershipHeaderName", "cardOwnershipValidated")
        );
    }

    private UserProfile authenticate(Integer userId) {
        UserProfile profile = new UserProfile("user1", "person1", userId);
        TestAuthentication authentication = new TestAuthentication(profile);
        authentication.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return profile;
    }

    private static class TestAuthentication extends AbstractAuthenticationToken implements Authentication {

        private final UserProfile profile;

        TestAuthentication(UserProfile profile) {
            super(List.of());
            this.profile = profile;
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
        public boolean isFullyAuthenticated() {
            return isAuthenticated();
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
        public Boolean getIsTransactionAuthenticated() {
            return false;
        }

        @Override
        public void authenticateTransaction(boolean isTransactionAuthenticated) {
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return "user1";
        }
    }
}
