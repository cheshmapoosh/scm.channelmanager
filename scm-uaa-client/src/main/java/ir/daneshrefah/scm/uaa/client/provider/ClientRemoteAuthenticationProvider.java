package ir.daneshrefah.scm.uaa.client.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

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
public class ClientRemoteAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {

    private final JwtTokenConverter jwtTokenConverter;
    private final JwtDecoder jwtDecoder;
    private final ObjectMapper objectMapper;

    public ClientRemoteAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider,
                                              JwtDecoder jwtDecoder,
                                              ObjectMapper objectMapper,
                                              SessionCache sessionCache,
                                              CacheTemplate cacheTemplate) {
        super(remoteSecurityServiceProvider, sessionCache, cacheTemplate);
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
//        UserDetails loadedUser = getSessionCache().getUserFromCache(authentication.getId());
//        if (loadedUser == null) {
//            throw new InternalAuthenticationServiceException(
//                    "userCache returned null, which is an interface contract violation");
//        }
        return userAuthentication;
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
        return ClientAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
