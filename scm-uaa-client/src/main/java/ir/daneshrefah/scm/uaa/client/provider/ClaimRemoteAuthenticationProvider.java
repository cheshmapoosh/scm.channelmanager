package ir.daneshrefah.scm.uaa.client.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.client.converter.token.TokenConverter;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClaimAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
@ConditionalOnProperty(name = "scm.security.distributed", havingValue = "true", matchIfMissing = false)
@Component
public class ClaimRemoteAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {

    private final TokenConverter<String> jwtAuthenticationConverter;
    private final ObjectMapper objectMapper;

    public ClaimRemoteAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider,
                                             TokenConverter<String> jwtAuthenticationConverter,
                                             ObjectMapper objectMapper,
                                             SessionCache sessionCache) {
        super(remoteSecurityServiceProvider, sessionCache);
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.objectMapper = objectMapper;
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        String authenticationResult = remoteSecurityServiceProvider.authenticateClaim((ClaimAuthenticationToken) authentication);
        if (null == authenticationResult) {
            throw new UsernameNotFoundException(
                    "remoteServiceProvider returned null, which is an interface contract violation");
        }
//        String accessToken = extractAccessToken(authenticationResult);
//        UserAuthentication userAuthentication = jwtAuthenticationConverter.convert(accessToken);

//        return userAuthentication;
        return new UserAuthentication(null, null, Collections.emptyList());
    }

    private String extractAccessToken(String value) {
        Map<String, String> responseMap = null;
        try {
            responseMap = objectMapper.readValue(value, Map.class);
        } catch (JsonProcessingException e) {
            logger.error("invalid response: " + value, e);
            return null;
        }
        return responseMap.get("access_token");
    }

    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClaimAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
