package ir.daneshrefah.scm.uaa.controller.token;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Use this api only on development environment.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/access-token")
@Tag(name = "ACCESS TOKEN", description = "Generate access token for dev environment")
public class AccessTokenController {

    @GetMapping("/get-first-password-token")
    public ResponseEntity<?> getToken(
            @Nullable @RequestParam("uaa-server-url") String uaaServerUrl,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("terminal") String terminal,
            @RequestParam("access-parameter") String accessParameter) {
        if (Objects.isNull(uaaServerUrl)) {
            uaaServerUrl = "http://localhost:8000/oauth2/token";
        }
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/x-www-form-urlencoded");
        HttpEntity<String> requestEntity = new HttpEntity<>(getBody(username,password,terminal,accessParameter), headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
                uaaServerUrl,
                HttpMethod.POST,
                requestEntity,
                String.class
        );
        return ResponseEntity.ok(response.getBody());
    }

    public String getBody(String username, String password, String terminal, String accessParameter) {
        return  String.format(
                "grant_type=%s&username=%s&password=%s&client_id=%s&access_parameter=%s",
                URLEncoder.encode("first_password", StandardCharsets.UTF_8),
                URLEncoder.encode(username, StandardCharsets.UTF_8),
                URLEncoder.encode(password, StandardCharsets.UTF_8),
                URLEncoder.encode(terminal, StandardCharsets.UTF_8),
                URLEncoder.encode(accessParameter, StandardCharsets.UTF_8)
        );
    }

}
