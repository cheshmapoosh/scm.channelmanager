package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.converter;

import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyPasswordGrantAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.DefaultGrantPreAuthenticationToken;
import ir.daneshrefah.scm.uaa.utils.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Legacy default-grant mapper kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
@SuppressWarnings("removal")
public class LegacyDefaultGrantRequestMapper {
    public void applyCommonFields(
            LegacyPasswordGrantAuthenticationToken token,
            HttpServletRequest request,
            LegacyPasswordGrantRequestMapper.ParameterSearch parameters
    ) {
        token.setAccessParameter(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACCESS_PARAMETER));
        token.setClaimCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_CLAIM));
        token.setClientVersion(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION));
        token.setClientSignature(request.getParameter(Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE));
        token.setActivationCode(request.getParameter(Constants.OAUTH2_PARAM_NAME_USER_REGISTER_CODE));
        token.setActivatorTerminal(request.getParameter(Constants.OAUTH2_PARAM_NAME_ACTIVATOR_TERMINAL));
        parameters.getFirst(Constants.APP_VERSION_HEADER).ifPresent(token::setClientVersion);
        parameters.getFirst(Constants.SIGNATURE_HEADER).ifPresent(token::setClientSignature);
        parameters.getFirst(Constants.ACCESS_PARAM_HEADER).ifPresent(token::setAccessParameter);
    }

    public void applyDefaultGrantFields(
            LegacyPasswordGrantAuthenticationToken token,
            HttpServletRequest request,
            LegacyPasswordGrantRequestMapper.ParameterSearch parameters
    ) {
        DefaultGrantPreAuthenticationToken defaultGrantToken = new DefaultGrantPreAuthenticationToken();
        parameters.getFirst(Constants.CHANNEL_HEADER).ifPresent(defaultGrantToken::setChannel);
        parameters.getFirst(Constants.APP_VERSION_HEADER).ifPresent(defaultGrantToken::setAppVersion);
        parameters.getFirst(Constants.SIGNATURE_HEADER).ifPresent(defaultGrantToken::setSignature);
        parameters.getFirst(Constants.ACCESS_PARAM_HEADER).ifPresent(defaultGrantToken::setAccessParam);
        parameters.getFirst(Constants.HASHCODE_HEADER).ifPresent(defaultGrantToken::setHashcode);
        parameters.getFirst(Constants.AGENT_HEADER).ifPresent(defaultGrantToken::setAgent);
        parameters.getFirst(Constants.REGISTRY_TOKEN_HEADER).ifPresent(defaultGrantToken::setRegistryToken);
        parameters.getFirst(Constants.OPERATING_SYSTEM_VERSION_HEADER).ifPresent(defaultGrantToken::setOperationSystemVersion);
        parameters.getFirst(Constants.DEVICE_MODEL_HEADER).ifPresent(defaultGrantToken::setDeviceModel);
        parameters.getFirst(Constants.UUID_HEADER).ifPresent(defaultGrantToken::setUuid);
        parameters.getFirst(Constants.PWA_OTP_CODE_HEADER).ifPresent(defaultGrantToken::setOtpCode);
        parameters.getFirst(Constants.PWA_OTP_CODE_HEADER).ifPresent(token::setClaimCode);
        parameters.getFirst(Constants.PWA_TERMINAL_TYPE_HEADER).ifPresent(defaultGrantToken::setTerminalType);
        defaultGrantToken.setIp(RequestUtils.getOrDefaultRequestIp(null, request));
        token.setDefaultGrantPreAuthToken(defaultGrantToken);
    }
}
