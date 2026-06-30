package ir.daneshrefah.scm.uaa.security.oauth2.policy;

import ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.LegacyAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Central place for legacy grant enablement decisions.
 */
@Component
@RequiredArgsConstructor
@SuppressWarnings("removal")
public class RegisteredClientLegacyPolicy {
    private final LegacyAuthProperties properties;

    public boolean legacyPasswordGrantEnabled() {
        return properties.isEnabled();
    }
}
