package ir.daneshrefah.scm.uaa.client.remote;

import com.hazelcast.internal.ascii.rest.HttpStatusCode;
import ir.daneshrefah.scm.uaa.client.provider.token.BasicAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClaimAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.utils.string.HttpConstants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
@Component
@ConditionalOnProperty(name = "scm.security.distributed", havingValue = "true", matchIfMissing = false)
public class RemoteSecurityServiceProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    private final RestTemplate restTemplate;
    @Value("${scm.security.token-endpoint}")
    private String tokenEndpoint;
    private final JwtDecoder jwtDecoder;

    public RemoteSecurityServiceProvider(RestTemplate restTemplate, JwtDecoder jwtDecoder) {
        this.restTemplate = restTemplate;
        this.jwtDecoder = jwtDecoder;
    }

    public String authenticateClaim(ClaimAuthenticationToken authentication) throws AuthenticationException {
        return (String) authentication.getCredentials();
    }

    public String authenticateClient(ClientAuthenticationToken authentication) throws AuthenticationException {
        String headerAuthorization = "";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (StringUtils.isNotEmpty(headerAuthorization)) {
            headers.set("Authorization", headerAuthorization); // Add authorization header
        }

        // Create form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add(OAuth2ParameterNames.GRANT_TYPE, AuthorizationGrantType.CLIENT_CREDENTIALS.getCode());
        formData.add(OAuth2ParameterNames.CLIENT_ID, (String) authentication.getPrincipal());
        formData.add(OAuth2ParameterNames.CLIENT_SECRET, (String) authentication.getCredentials());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        int statusCode = 0;
        String responseBody = null;
        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, requestEntity, String.class);
            statusCode = response.getStatusCode().value();
            responseBody = response.getBody();
        } catch (RestClientResponseException e) {
            logger.error("response error on remote authenticate for user: " + authentication.getPrincipal(), e);
            statusCode = e.getStatusCode().value();
        } catch (RestClientException e) {
            logger.error("error on remote authenticate for user: " + authentication.getPrincipal(), e);
            throw new AuthenticationServiceException("error on client authentication.", e);
//            statusCode = HttpConstants.HTTP_STATUS_BAD_REQUEST;
        }
        boolean isAuthenticated = HttpConstants.HTTP_STATUS_OK == statusCode/* ||
                HttpStatusCode.SC_204.equals(statusCode)*/;
        if (!isAuthenticated) {
            return null;
        }
//        Jwt jwt = getJwt(response.getBody());
        return responseBody;
    }

    public BasicAuthenticationToken authenticateBasic(BasicAuthenticationToken authentication) throws AuthenticationException {
        String headerAuthorization = "";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        if (StringUtils.isNotEmpty(headerAuthorization)) {
            headers.set("Authorization", headerAuthorization); // Add authorization header
        }

        // Create form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", AuthorizationGrantType.FIRST_PASSWORD.getCode());
        formData.add("username", (String) authentication.getPrincipal());
        formData.add("password", (String) authentication.getCredentials());

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(formData, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(tokenEndpoint, requestEntity, String.class);

        boolean isAuthenticate = HttpStatusCode.SC_200.equals(response.getStatusCode()) ||
                HttpStatusCode.SC_204.equals(response.getStatusCode());
        if (!isAuthenticate) {
            return null;
        }
        return null;
    }

    private Jwt getJwt(String token) {
        try {
            return this.jwtDecoder.decode(token);
        }
        catch (BadJwtException failed) {
            this.logger.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        }
        catch (JwtException failed) {
            throw new AuthenticationServiceException(failed.getMessage(), failed);
        }
    }

}
