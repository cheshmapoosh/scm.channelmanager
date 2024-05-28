package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.security.authenticationProvider.providers.AbstractAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderNotFoundException;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class DelegatorAuthenticationProvider implements AuthenticationProvider {

    private final List<AbstractAuthenticationProvider> providers;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DelegatorAuthenticationProvider(ApplicationEventPublisher applicationEventPublisher,
                                           List<AbstractAuthenticationProvider> providers) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.providers = providers;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Class<? extends Authentication> toTest = authentication.getClass();
        Authentication result = null;

        for (AuthenticationProvider provider : providers) {
            if (!provider.supports(toTest)) {
                continue;
            }

            result = provider.authenticate(authentication);
            if (Objects.nonNull(result))
                break;
        }

        if (Objects.isNull(result))
            throw new ProviderNotFoundException("DelegatorAuthenticationProvider.providerNotFound");

        if (result.isAuthenticated()) {
            applicationEventPublisher.publishEvent(new AuthenticationSuccessEvent(result));
        }

        return result;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GeneralAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
