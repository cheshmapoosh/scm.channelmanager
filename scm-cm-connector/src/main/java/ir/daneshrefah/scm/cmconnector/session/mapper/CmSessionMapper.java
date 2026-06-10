package ir.daneshrefah.scm.cmconnector.session.mapper;

import ir.daneshrefah.scm.cmconnector.session.model.CmSessionResponse;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class CmSessionMapper {

    public CmSessionResponse toResponse(UserAuthentication authentication) {
        UserAuthentication.AuthenticationDetail details = authentication.getDetails();
        if (details == null) {
            throw new IllegalStateException("Cached session is missing authentication details");
        }
        return new CmSessionResponse(
                details.getIssuer(),
                details.getSessionId(),
                epochMillis(details.getIssuedAt()),
                epochMillis(details.getExpiresAt()),
                seconds(details.getMaxIdle()),
                authentication.getAuthenticationMethod() == null ? null : authentication.getAuthenticationMethod().name(),
                details.getLoginAccessParameter()
        );
    }

    private Long epochMillis(Instant value) {
        return value == null ? null : value.toEpochMilli();
    }

    private Integer seconds(Duration value) {
        return value == null ? null : Math.toIntExact(value.toSeconds());
    }
}
