package ir.daneshrefah.scm.uaa.security.oauth2.error;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShahkarOAuth2ErrorMapper {
    private final OAuth2AuthenticationErrorMapper errorMapper;

    public String safeMessage(Throwable throwable) {
        return errorMapper.safeMessage(throwable);
    }
}
