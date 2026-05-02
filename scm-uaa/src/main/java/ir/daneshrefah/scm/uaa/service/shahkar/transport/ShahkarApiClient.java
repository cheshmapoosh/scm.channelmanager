package ir.daneshrefah.scm.uaa.service.shahkar.transport;

import ir.daneshrefah.scm.uaa.service.shahkar.config.ShahkarProperties;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarInquiryRequest;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarInquiryResponse;
import ir.daneshrefah.scm.uaa.service.shahkar.domain.ShahkarTokenResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.StreamingHttpOutputMessage;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class ShahkarApiClient {

    private static final Logger log = LoggerFactory.getLogger(ShahkarApiClient.class);

    private final RestClient restClient;
    private final ShahkarProperties props;

    public ShahkarTokenResponse fetchToken() {
        // Placeholder: later adapt to real token request format (form-url-encoded, json, headers...)
        log.debug("Shahkar: fetching token from {}", props.getTokenPath());

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", props.getClientId());
        formData.add("client_secret", props.getClientSecret());

        return restClient.post()
                .uri(props.getBaseUrl()+props.getTokenPath())
                .contentType(new MediaType("application","x-www-form-urlencoded"))
                .accept(MediaType.APPLICATION_JSON)
                .body(formData)
                .retrieve()
                .body(ShahkarTokenResponse.class);
    }

    public ShahkarInquiryResponse inquiryOwnership(String accessToken, ShahkarInquiryRequest request) {
        log.debug("Shahkar: calling inquiry endpoint {}", props.getInquiryPath());

        return restClient.post()
                .uri(props.getBaseUrl()+props.getInquiryPath())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .body(request)
                .retrieve()
                .body(ShahkarInquiryResponse.class);
    }

    // placeholder token request body
    private record TokenRequestPlaceholder(String clientId, String clientSecret) {}
}