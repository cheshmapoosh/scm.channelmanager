package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
@Component
public class AuthenticationSessionEventListener extends BaseAuthenticationListener {

    @Override
    protected void onSuccessAuthenticationEvent(PostAuthenticationToken authentication) {
        if (!authentication.isSessionRequired()) {
            return;
        }
        authentication.setSessionKey(generateSessionKey(
                authentication.getName(), null,
                authentication.getTerminalCode()));
    }

    private String generateSessionKey(String username, String delegator, String terminalCode) {
        LOGGER.debug("Generating sessionKey for user: " + username);
        boolean delegatorNotEmpty = StringUtils.isNotEmpty(delegator);
        String mainUser = delegatorNotEmpty ? delegator : username;
        String secondaryUser = delegatorNotEmpty ? username : null;
        String plainSession = mainUser
                .concat(StringUtils.DOUBLE_COLON)
                .concat(Optional.ofNullable(secondaryUser).orElse("null"))
                .concat(StringUtils.DOUBLE_COLON)
                .concat(terminalCode)
                .concat(StringUtils.DOUBLE_COLON)
                .concat(UUID.randomUUID().toString());
        byte[] encodedBytes = Base64.getEncoder().encode(plainSession.getBytes());
        String encodedSession = new String(encodedBytes);
        return encodedSession;
    }

}
