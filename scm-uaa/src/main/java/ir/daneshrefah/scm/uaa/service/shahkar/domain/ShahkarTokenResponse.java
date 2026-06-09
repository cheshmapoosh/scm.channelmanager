package ir.daneshrefah.scm.uaa.service.shahkar.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ShahkarTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Long expiresInSeconds
) {}