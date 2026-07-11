package ir.daneshrefah.scm.core.integration.plugin.specific;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.handler.PluginHandler;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.error.ErrorCodes;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.plugin.PluginDetail;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.services.card.CardMaskingUtils;
import ir.daneshrefah.scm.core.services.card.CardOwnershipUserProfileCacheResult;
import ir.daneshrefah.scm.core.services.card.CardOwnershipUserProfileCacheService;
import ir.daneshrefah.scm.common.model.customer.UserProfileThreadLocal;
import ir.daneshrefah.scm.core.config.CardOwnershipUserProfileCachePluginConfig;
import ir.daneshrefah.scm.core.integration.plugin.support.PluginMessageValueReader;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.model.RouteDefinition;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CardOwnershipUserProfileCachePlugin implements PluginHandler {

    private final ObjectMapper objectMapper;
    private final PluginMessageValueReader valueReader;
    private final CardOwnershipUserProfileCacheService userProfileCacheService;

    @Override
    public PluginType getType() {
        return PluginType.VALIDATOR;
    }

    @Override
    public void init(RouteDefinition routeDefinition, PluginDetail pluginDetail, Map<String, ?> properties) {
        if (pluginDetail != null && pluginDetail.getPhase() != null && pluginDetail.getPhase() != PluginPhase.BEFORE) {
            throw new IllegalArgumentException("Plugin phase " + pluginDetail.getPhase()
                    + " is not supported on 'cardOwnershipUserProfileCachePlugin'");
        }
    }

    @Override
    public void handle(Exchange exchange, PluginDetail pluginDetail) {
        CardOwnershipUserProfileCachePluginConfig config = resolveConfig(pluginDetail);
        if (config == null || !config.isEnabled()) {
            return;
        }
        UserProfileThreadLocal.clear();

        Message message = exchange.getMessage();
        String cardNumber = normalizeCard(valueReader.findFirstValue(message, config.getCardNumberSources()));
        if (StringUtils.isBlank(cardNumber)) {
            if (config.isFailIfCardNotFound()) {
                throw new MissingRequiredInputException("CARD_NUMBER_NOT_FOUND");
            }
            markOwnership(message, config, false);
            return;
        }
        validateCardNumber(cardNumber);

        Authentication authentication = currentAuthentication();
        CardOwnershipUserProfileCacheResult result = userProfileCacheService.getValidatedUserProfile(
                config.getCacheName(),
                config.isCacheEnabled(),
                authentication,
                cardNumber,
                config.isFailIfCardNotFound(),
                config.isFailIfCardNotOwnedByUser());

        writeHeaders(message, config, cardNumber, result.userProfile(), result.validated());
        if (log.isDebugEnabled()) {
            log.debug("cardOwnershipUserProfileCachePlugin validated card={}", CardMaskingUtils.mask(cardNumber));
        }
    }

    private CardOwnershipUserProfileCachePluginConfig resolveConfig(PluginDetail pluginDetail) {
        if (pluginDetail == null) {
            log.debug("cardOwnershipUserProfileCachePlugin skipped because pluginDetail is null");
            return null;
        }
        Map<String, ?> rawConfig = pluginDetail.getConfig();
        if (rawConfig == null || rawConfig.isEmpty()) {
            return new CardOwnershipUserProfileCachePluginConfig();
        }
        try {
            return objectMapper.convertValue(rawConfig, CardOwnershipUserProfileCachePluginConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not convert cardOwnershipUserProfileCachePlugin database config. plugin="
                    + pluginDetail.getName(), e);
        }
    }

    private Authentication currentAuthentication() {
        Authentication authentication = AuthenticationUtils.getScmAuthentication();
        if (authentication == null || !authentication.isFullyAuthenticated() || authentication.getProfile() == null) {
            throw new AccessDeniedException("CURRENT_USER_NOT_RESOLVED", ErrorCodes.ERROR_CODE_ACCESS_DENIED, "CURRENT_USER_NOT_RESOLVED");
        }
        return authentication;
    }

    private String normalizeCard(String cardNumber) {
        return StringUtils.deleteWhitespace(cardNumber);
    }

    private void validateCardNumber(String cardNumber) {
        if (!cardNumber.matches("\\d{16,19}")) {
            throw new InvalidInputException("CARD_NUMBER_NOT_FOUND");
        }
    }

    private void writeHeaders(Message message,
                              CardOwnershipUserProfileCachePluginConfig config,
                              String cardNumber,
                              UserProfile profile,
                              boolean ownershipValidated) {
        if (StringUtils.isNotBlank(config.getCardNumberHeaderTarget())) {
            message.setHeader(config.getCardNumberHeaderTarget(), cardNumber);
        }
        if (config.isPutUserProfileInHeader()) {
            message.setHeader(config.getUserProfileHeaderName(), profile);
        }
        markOwnership(message, config, ownershipValidated);
    }

    private void markOwnership(Message message, CardOwnershipUserProfileCachePluginConfig config, boolean ownershipValidated) {
        if (config.isPutOwnershipResultInHeader()) {
            message.setHeader(config.getOwnershipHeaderName(), ownershipValidated);
        }
    }
}
