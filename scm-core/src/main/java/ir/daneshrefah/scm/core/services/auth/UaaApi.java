package ir.daneshrefah.scm.core.services.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class UaaApi {
    private final RestTemplate restTemplate;

    public void removeXUserByUsername(Integer userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        log.info("Calling UAA (api/x-user-detail/remove): userId={}", userId);
        restTemplate.exchange(
                "http://localhost:8000/api/x-user-detail/remove?userId=" + userId,
                HttpMethod.GET,
                request,
                Void.class
        );
    }
}
