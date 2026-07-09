package ir.daneshrefah.scm.uaa.starter.security;

import ir.daneshrefah.scm.uaa.starter.properties.ScmResourceServerProperties;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class ScmJwtAuthenticationConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {
    private final ScmResourceServerProperties properties;

    public ScmJwtAuthenticationConverter(ScmResourceServerProperties properties) {
        this.properties = properties;
    }

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt jwt) {
        validateRequiredClaims(jwt);
        ScmResourceServerProperties.Claims claims = properties.getClaims();
        Set<String> roles = claimValues(jwt.getClaim(claims.getRoles()));
        Set<String> scopes = claimValues(jwt.getClaim(claims.getScopes()));
        Set<GrantedAuthority> authorities = authorities(roles, scopes);
        ScmPrincipal principal = new ScmPrincipal(
                jwt.getClaimAsString(claims.getSubject()),
                jwt.getClaimAsString(claims.getSessionId()),
                jwt.getClaimAsString(claims.getNickname()),
                jwt.getClaimAsString(claims.getTerminalCode()),
                jwt.getClaimAsString(claims.getClientId()),
                jwt.getClaimAsString(claims.getTokenId()),
                roles,
                scopes
        );
        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    private void validateRequiredClaims(Jwt jwt) {
        for (String requiredClaim : properties.getRequiredClaims()) {
            if (requiredClaim == null || requiredClaim.isBlank()) {
                continue;
            }
            Object value = jwt.getClaim(requiredClaim.trim());
            if (value == null || value instanceof String text && text.isBlank()) {
                throw new BadCredentialsException("JWT is missing required SCM claim: " + requiredClaim.trim());
            }
        }
    }

    private Set<GrantedAuthority> authorities(
            Set<String> roles,
            Set<String> scopes
    ) {
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        for (String role : roles) {
            String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(authority));
        }
        for (String scope : scopes) {
            authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
        }
        return authorities;
    }

    private Set<String> claimValues(Object value) {
        if (value == null) {
            return Set.of();
        }
        if (value instanceof String text) {
            return split(text);
        }
        if (value instanceof Collection<?> values) {
            Set<String> result = new LinkedHashSet<>();
            for (Object item : values) {
                if (item != null) {
                    String text = String.valueOf(item).trim();
                    if (!text.isBlank()) {
                        result.add(text);
                    }
                }
            }
            return result;
        }
        return Set.of();
    }

    private Set<String> split(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        Set<String> values = new LinkedHashSet<>();
        for (String value : text.split("[,\\s]+")) {
            if (!value.isBlank()) {
                values.add(value.trim());
            }
        }
        return values;
    }
}
