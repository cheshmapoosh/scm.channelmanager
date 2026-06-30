package ir.daneshrefah.scm.uaa.client.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
public class ClientRemoteAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {

    private final JwtTokenConverter jwtTokenConverter;
    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;

    public ClientRemoteAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider,
                                              JwtDecoder jwtDecoder,
                                              ObjectMapper objectMapper,
                                              SessionCache sessionCache,
                                              CacheManager cacheManager) {
        super(remoteSecurityServiceProvider, sessionCache, cacheManager);
        this.jwtDecoder = jwtDecoder;
        this.jwtTokenConverter = new JwtTokenConverter();
        this.objectMapper = objectMapper;
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        String authenticationResult = remoteSecurityServiceProvider.authenticateClient((ClientAuthenticationToken) authentication);
        if (null == authenticationResult) {
            throw new UsernameNotFoundException(
                    "remoteServiceProvider returned null, which is an interface contract violation");
        }
        String accessToken = extractAccessToken(authenticationResult);
        Jwt jwt = jwtDecoder.decode(accessToken);
        UserAuthentication userAuthentication = jwtTokenConverter.convert(jwt);
        return userAuthentication;
    }

    private String extractAccessToken(String value) {
        Map<String, String> responseMap = null;
        try {
            responseMap = objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            logger.error("invalid remote client authentication response: " + safeMessage(e));
            return null;
        }
        return responseMap.get("access_token");
    }

    private String safeMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return exception == null ? null : exception.getClass().getSimpleName();
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|cookie)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
