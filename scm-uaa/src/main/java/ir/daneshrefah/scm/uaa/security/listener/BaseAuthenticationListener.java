package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
public abstract class BaseAuthenticationListener implements ApplicationListener<AuthenticationSuccessEvent> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();
        if (null == authentication || !AuthenticationOutcomeToken.class.isAssignableFrom(authentication.getClass())) {
            return;
        }
        AuthenticationOutcomeToken postAuthenticationToken = (AuthenticationOutcomeToken) authentication;
        onSuccessAuthenticationEvent(postAuthenticationToken);
    }

    protected abstract void onSuccessAuthenticationEvent(AuthenticationOutcomeToken authentication);

}
