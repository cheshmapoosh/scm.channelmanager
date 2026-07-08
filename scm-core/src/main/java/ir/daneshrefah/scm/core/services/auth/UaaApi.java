package ir.daneshrefah.scm.core.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UaaApi {
    private final RestTemplate restTemplate;

    public void removeXUserByUsername(Integer userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJkYW5lc2giLCJ0YW0iOiJTTVMiLCJhdXQiOlsiUk9MRV9DT1JQT1JBVEVfQ1VTVE9NRVIiLCJST0xFX0NVU1RPTUVSIl0sImdybiI6IkZJUlNUX1BBU1NXT1JEIiwicHNpIjoiMCIsImlzcyI6Imh0dHA6Ly9zY20tYXV0aC1zZXJ2ZXIiLCJwcGkiOiIyMjg4NDI2NDIyIiwicHRsIjoi2K_Yp9mG2LQg2LHZgdin2Ycg2b7Ysdiv24zYsyIsInBpZCI6MjIwMzcyMDAsInBuaSI6IjE0MDA2Mjg0ODY0IiwidHRsIjoyMDAsInRybSI6Ik5JQiIsInBwbiI6IjA5MzAqKio3NjAxIiwiYWNwIjoiMDkzMDE2Nzc2MDEiLCJwYmMiOiIwMDAxMTAiLCJhdWQiOiJOSUIiLCJuYmYiOjE3ODM1MTkxODIsInB0eSI6MywicG50IjoiSSIsImxhbSI6IlNQRCIsIm1pdCI6MjAwLCJleHAiOjE3ODM1MzExODIsImlhdCI6MTc4MzUxOTE4MiwianRpIjoiNWMzZGJhMjctMGMxMi00ZGY0LTk5YTAtMDg5YzVhZmMxMzcwIn0.GiVAVJcz35-Kn5aBoyNqtF5WOqZX3S0-y6XJX6wg0ahvXj9yGiHgd5A_TbuSisQiR7HjgO1fPNaZ6lPy4CumPamwNWdECGiy4D0dYrxHl2KigngTnMAm3WTGrcGZ9C3gCjs1POVxi75rrkcmvDfPLxOX62VsBbPFBtSO5oO4fZz4p5MRqTJ2cAO-LLQ3abrGpCwxgqaTltG13U718FhC_6gfHwpKg-QSVNRoU4s1qwW-ztyS1TD0N_Ztn6I_8S3PzcXHAFna9sbGx7QKZ3EdzOh11aZIZSzv-CMQRKf1HiazZ2Xjzb8yYULd1moF6tD4o8jCNF9Pmphcx6_tmElI5w");

        HttpEntity<Void> request = new HttpEntity<>(headers);

        restTemplate.exchange(
                "http://localhost:8000/api/x-user-detail/remove?userId=" + userId,
                HttpMethod.GET,
                request,
                Void.class
        );
    }
}
