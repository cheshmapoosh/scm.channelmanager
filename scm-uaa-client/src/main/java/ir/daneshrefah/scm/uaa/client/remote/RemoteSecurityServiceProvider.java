package ir.daneshrefah.scm.uaa.client.remote;

import com.hazelcast.internal.ascii.rest.HttpStatusCode;
import ir.daneshrefah.scm.uaa.client.provider.token.BasicAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
@Component
public class RemoteSecurityServiceProvider {

    private final RestTemplate restTemplate;
    @Value("${uaa.server.token-endpoint}")
    private String tokenEndpoint;

    public RemoteSecurityServiceProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean authenticateBasic(BasicAuthenticationToken authentication) throws AuthenticationException {
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

        return HttpStatusCode.SC_200.equals(response.getStatusCode()) ||
                HttpStatusCode.SC_204.equals(response.getStatusCode());
    }

}
