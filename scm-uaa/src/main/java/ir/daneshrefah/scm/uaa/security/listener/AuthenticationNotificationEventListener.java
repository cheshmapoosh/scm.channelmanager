package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
@Component
public class AuthenticationNotificationEventListener extends BaseAuthenticationListener {

    @Override
    protected void onSuccessAuthenticationEvent(PostAuthenticationToken authentication) {
        if (!authentication.isNotificationRequired()) {
            return;
        }

    }

}
