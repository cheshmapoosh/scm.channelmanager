package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.security.authenticationProvider.provider.AbstractAuthenticationProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class DelegatorAuthenticationProvider implements AuthenticationProvider {

    private List<AbstractAuthenticationProvider> providers = Collections.emptyList();

    public DelegatorAuthenticationProvider(ApplicationContext context) {
        Map<String, AbstractAuthenticationProvider> providersMap =
                context.getBeansOfType(AbstractAuthenticationProvider.class);

        this.providers = providersMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return false;
    }
}
