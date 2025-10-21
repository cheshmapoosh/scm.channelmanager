package ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralRealPersonEntity;
import ir.daneshrefah.scm.common.model.message.TokenType;
import ir.daneshrefah.scm.uaa.common.constants.AuthStatus;
import ir.daneshrefah.scm.uaa.common.constants.PwaOauthMessage;
import ir.daneshrefah.scm.uaa.common.utils.ErrorUtils;
import ir.daneshrefah.scm.uaa.config.PwaAuthenticationConfigProperties;
import ir.daneshrefah.scm.uaa.domain.pwa.PwaLogin;
import ir.daneshrefah.scm.uaa.domain.role.Role;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import ir.daneshrefah.scm.uaa.security.token.DefaultGrantPreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.activation.pwa.common.GeneralPwaOauthException;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.activation.UserActivationService;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.model.PwaOAuth2AccessToken;
import ir.daneshrefah.scm.uaa.service.client.ClientVersionService;
import ir.daneshrefah.scm.uaa.service.person.UPersonService;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.date.DateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_APP_VERSION;

@Slf4j
@Service
@RequiredArgsConstructor
public class PwaAuthenticationServiceImpl implements PwaAuthenticationService {

    private static final String OTP_ENABLE = "OTP_ENABLED";
    private static final String OTP_DISABLE = "OTP_DISABLED";
    private static final String GRN_CLAIM_VALUE = "password";

    private final PwaWhiteListService whiteListService;
    private final ClientVersionService clientVersionService;
    private final PwaUserLoginService userLoginService;
    private final UserActivationService userActivationService;
    private final UserService userService;
    private final UPersonService uPersonService;
    private final PwaAuthenticationConfigProperties properties;
    private final JwtDecoder jwtDecoder;


    @Override
    public void preAuthenticateCheck(PreAuthenticationToken token) throws OAuth2AuthenticationException {
        DefaultGrantPreAuthenticationToken pwaTokenInfo = token.getDefaultGrantPreAuthToken();
        whiteListService.checkWhiteList(token.getUsername());
        if (!clientVersionService.isAppSignatureValid(pwaTokenInfo.getAppVersion(), pwaTokenInfo.getSignature())) {
            ErrorUtils.throwError(OAUTH2_ERROR_CODE_INVALID_APP_VERSION, PwaOauthMessage.CLIENT_INVALID_APP_VERSION.name());
        }
        userLoginService.checkIfBlocked(token.getUsername());
        userActivationService.checkActivationIfNeeded(token);
    }

    @Override
    public PwaOAuth2AccessToken postAuthenticate(PreAuthenticationToken token, TokenType tokenType, String jwt, int expireIn) {
        PwaOAuth2AccessToken accessToken = new PwaOAuth2AccessToken();
        accessToken.setAccessToken(jwt);
        accessToken.setRefreshToken(jwt);
        accessToken.setTokenType(tokenType.name().toLowerCase());
        accessToken.setExpiresIn(expireIn);
        accessToken.setHasRegistryTokenFromCookie(true);
        accessToken.setOtpStatus(OTP_DISABLE);
        accessToken.setGrn(GRN_CLAIM_VALUE);
        accessToken.setScope(OidcScopes.OPENID);
        accessToken.setAppVersion(token.getDefaultGrantPreAuthToken().getAppVersion());
        Jwt decode = jwtDecoder.decode(jwt);
        assert decode.getIssuedAt() != null;
        accessToken.setJti(decode.getClaim("jti"));
        accessToken.setIat(decode.getIssuedAt().toEpochMilli());
        accessToken.setExpirationDate(DateUtils.DateConverter.convertToDate(decode.getExpiresAt()));
        setUserSecurityDetails(token, accessToken);
        userLoginService.succeededTrial(createPwaLogin(token));
        return accessToken;
    }

    private PwaLogin createPwaLogin(PreAuthenticationToken token) {
        final PwaLogin login = new PwaLogin();
        getUserByUsername(token.getUsername(), StringUtils.toRootUpperCase(token.getDefaultGrantPreAuthToken().getChannel()))
                .map(UserEntity::getPerson)
                .ifPresent(person -> login.setRealUsername(person.getUsername()));
        DefaultGrantPreAuthenticationToken defaultGrantAuth = token.getDefaultGrantPreAuthToken();
        login.setAgent(defaultGrantAuth.getAgent());
        login.setPhoneNumber(defaultGrantAuth.getAccessParam());
        login.setUsername(token.getUsername());
        login.setDeviceModel(defaultGrantAuth.getDeviceModel());
        login.setStatus(AuthStatus.SUCCEEDED);
        login.setLastLogin(ZonedDateTime.now());
        login.setIp(defaultGrantAuth.getIp());
        login.setChannelCode(defaultGrantAuth.getChannel().toUpperCase());
        return login;
    }

    @Override
    public void checkLoginTrails(PreAuthenticationToken preAuthenticationToken) {
        PwaLogin pwaLogin = createPwaLogin(preAuthenticationToken);
        if (userLoginService.hasReachedLoginLimit(pwaLogin)){
            throw new GeneralPwaOauthException(PwaOauthMessage.REACHED_LOGIN_LIMIT);
        }
    }

    @Override
    public PwaOAuth2AccessToken createTwoPhaseLoginResponse(PreAuthenticationToken token) {
        final PwaOAuth2AccessToken resp = new PwaOAuth2AccessToken();
        resp.setAccessToken(null);
        resp.setRefreshToken(null);
        resp.setScope(OidcScopes.OPENID);
        resp.setTokenType(TokenType.BEARER.name().toLowerCase());
        resp.setIat(System.currentTimeMillis());
        resp.setOtpStatus(OTP_ENABLE);
        resp.setJti(UUID.randomUUID().toString());
        resp.setHasRegistryTokenFromCookie(true);
        resp.setAppVersion(token.getDefaultGrantPreAuthToken().getAppVersion());
        setUserSecurityDetails(token, resp);
        return resp;
    }

    private Optional<UserEntity> getUserByUsername(String username, String channelCode) {
        return userService
                .findByNicknameAndLegacyTerminalCode(username, channelCode)
                .stream()
                .findFirst();
    }

    private void setUserSecurityDetails(PreAuthenticationToken token, PwaOAuth2AccessToken resp) {
        getUserByUsername(token.getUsername(), StringUtils.toRootUpperCase(token.getDefaultGrantPreAuthToken().getChannel()))
                .ifPresent(user -> {
                    GeneralPersonEntity person = user.getPerson();
                    if (person instanceof GeneralRealPersonEntity realPerson) {
                        resp.setFirstName(realPerson.getFirstName());
                        resp.setLastName(realPerson.getLastName());
                        String roles = uPersonService.findPersonRoleList(realPerson.getId()).stream().map(Role::getCode).collect(Collectors.joining(","));
                        resp.setAut(roles);
                        resp.setRealUsername(realPerson.getUsername());
                    }
                    resp.setSecondAuthenticationMethod(user.getTransactionAuthenticationMethod());
                    resp.setLastChangePassword(DateUtils.ShamsiCalendarConvertor.convertToShamsiDateString(user.getLastEditDate(), "yyyy/MM/dd HH:MM"));
                    resp.setWarnUserToChangePassword(checkUserChangePasswordWarn(user.getLastEditDate()));
                    resp.setGrn(GRN_CLAIM_VALUE);
                });
    }

    private String checkUserChangePasswordWarn(LocalDateTime lastEditDate) {
        Integer warnPeriodDays = properties.getLogin().tokenChangeWarnPeriodDays();
        if (lastEditDate.plusDays(warnPeriodDays).isBefore(LocalDateTime.now())) {
            return Boolean.TRUE.toString().toLowerCase();
        }
        return Boolean.FALSE.toString().toLowerCase();
    }
}
