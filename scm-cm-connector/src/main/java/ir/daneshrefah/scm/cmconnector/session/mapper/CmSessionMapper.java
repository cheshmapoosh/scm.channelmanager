package ir.daneshrefah.scm.cmconnector.session.mapper;

import ir.daneshrefah.scm.cmconnector.session.model.CmSessionResponse;
import ir.daneshrefah.scm.uaa.starter.session.ScmSessionView;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class CmSessionMapper {

    public CmSessionResponse toResponse(ScmSessionView session) {
        return new CmSessionResponse(
                session.issuer(),
                session.sessionId(),
                epochMillis(session.issuedAt()),
                epochMillis(session.expiresAt()),
                seconds(session.maxIdle()),
                session.loginAuthenticationMethod(),
                session.loginAccessParameter()
        );
    }

    private Long epochMillis(Instant value) {
        return value == null ? null : value.toEpochMilli();
    }

    private Integer seconds(Duration value) {
        return value == null ? null : Math.toIntExact(value.toSeconds());
    }
}
