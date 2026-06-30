package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter;

import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyPasswordGrantAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.DefaultGrantPreAuthenticationToken;
import ir.daneshrefah.scm.uaa.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Legacy default-grant mapper kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
@Component
public class LegacyDefaultGrantRequestMapper {
    public void applyCommonFields(
            LegacyPasswordGrantAuthenticationToken token,
            HttpServletRequest request,
            LegacyRequestParameters parameters
    ) {
        token.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        token.setClaimCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_CLAIM));
        token.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
        token.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
        token.setActivationCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_REGISTER_CODE));
        token.setActivatorTerminal(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACTIVATOR_TERMINAL));
        parameters.appVersion().ifPresent(token::setClientVersion);
        parameters.first(Constants.SIGNATURE_HEADER).ifPresent(token::setClientSignature);
        parameters.first(Constants.ACCESS_PARAM_HEADER).ifPresent(token::setAccessParameter);
    }

    public void applyDefaultGrantFields(
            LegacyPasswordGrantAuthenticationToken token,
            HttpServletRequest request,
            LegacyRequestParameters parameters
    ) {
        DefaultGrantPreAuthenticationToken defaultGrantToken = new DefaultGrantPreAuthenticationToken();
        parameters.first(Constants.CHANNEL_HEADER).ifPresent(defaultGrantToken::setChannel);
        parameters.appVersion().ifPresent(defaultGrantToken::setAppVersion);
        parameters.first(Constants.SIGNATURE_HEADER).ifPresent(defaultGrantToken::setSignature);
        parameters.first(Constants.ACCESS_PARAM_HEADER).ifPresent(defaultGrantToken::setAccessParam);
        parameters.first(Constants.HASHCODE_HEADER).ifPresent(defaultGrantToken::setHashcode);
        parameters.first(Constants.AGENT_HEADER).ifPresent(defaultGrantToken::setAgent);
        parameters.first(Constants.REGISTRY_TOKEN_HEADER).ifPresent(defaultGrantToken::setRegistryToken);
        parameters.first(Constants.OPERATING_SYSTEM_VERSION_HEADER).ifPresent(defaultGrantToken::setOperationSystemVersion);
        parameters.first(Constants.DEVICE_MODEL_HEADER).ifPresent(defaultGrantToken::setDeviceModel);
        parameters.first(Constants.UUID_HEADER).ifPresent(defaultGrantToken::setUuid);
        parameters.first(Constants.PWA_OTP_CODE_HEADER).ifPresent(defaultGrantToken::setOtpCode);
        parameters.first(Constants.PWA_OTP_CODE_HEADER).ifPresent(token::setClaimCode);
        parameters.first(Constants.PWA_TERMINAL_TYPE_HEADER).ifPresent(defaultGrantToken::setTerminalType);
        defaultGrantToken.setIp(RequestUtils.getOrDefaultRequestIp(null, request));
        token.setDefaultGrantPreAuthToken(defaultGrantToken);
    }
}
