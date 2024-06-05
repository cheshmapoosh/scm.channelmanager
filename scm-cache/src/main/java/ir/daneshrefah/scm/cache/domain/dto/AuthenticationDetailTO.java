package ir.daneshrefah.scm.cache.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;
@Getter
@Setter
public class AuthenticationDetailTO implements Serializable {
    private String issuer;
    private Instant issuedAt;
    private Instant expiresAt;
    private Duration maxIdle;
    private Object loginData;
    private String loginAccessParameter;
    private String sessionId;
    private String clientId;

}
