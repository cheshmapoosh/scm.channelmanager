package ir.daneshrefah.scm.cache.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema
public class UserAuthenticationTO {
    private String terminalCode;
    private String nickName;
    private String loginAuthenticationMethod;
    private String issuer;
    private String sessionId;
    private String loginAccessParameter;
    private Long expiresAt;
    private Long issuedAt;
    private int maxIdle;
    private int ttl;

}
