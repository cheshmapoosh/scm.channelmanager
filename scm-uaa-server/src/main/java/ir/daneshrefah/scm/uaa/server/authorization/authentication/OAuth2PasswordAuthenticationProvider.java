package ir.daneshrefah.scm.uaa.server.authorization.authentication;

import ir.daneshrefah.scm.uaa.server.security.crypto.password.UsernamePasswordEncoder;
import org.apache.commons.lang3.ClassUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class OAuth2PasswordAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public OAuth2PasswordAuthenticationProvider(UserDetailsService userDetailsService,
                                                PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = (String) authentication.getCredentials();

        UserDetails user = userDetailsService.loadUserByUsername(username);

        if (user == null || !matchesPassword(password, user)) {
            throw new BadCredentialsException("Invalid username or password");
        }

        return new UsernamePasswordAuthenticationToken(username, password, user.getAuthorities());
    }

    private boolean matchesPassword(String rawPassword, UserDetails user) {
        if (ClassUtils.isAssignable(passwordEncoder.getClass(), UsernamePasswordEncoder.class)) {
            return ((UsernamePasswordEncoder) passwordEncoder).matches(rawPassword, user.getPassword(), user.getUsername());
        }
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
