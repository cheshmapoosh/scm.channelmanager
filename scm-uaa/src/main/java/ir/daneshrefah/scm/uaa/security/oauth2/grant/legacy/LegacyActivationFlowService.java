package ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy;

import org.springframework.stereotype.Service;

/**
 * Legacy activation flow marker kept only for old NIB/PWA/MB compatibility.
 * Remove this class after migration to UAA-hosted login and authorization-code flow is complete.
 * No new feature should be added here unless strictly required for migration safety.
 */
@Service
@Deprecated(since = "9.0.0", forRemoval = true)
public class LegacyActivationFlowService {
}
