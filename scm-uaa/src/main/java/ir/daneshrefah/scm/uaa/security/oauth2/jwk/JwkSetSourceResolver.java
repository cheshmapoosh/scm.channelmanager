package ir.daneshrefah.scm.uaa.security.oauth2.jwk;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.ParseException;
import java.util.Locale;

/**
 * Resolves trusted, server-configured JWK Set locations into Nimbus key sources.
 */
@Component
public class JwkSetSourceResolver {

    private final ResourceLoader resourceLoader;

    public JwkSetSourceResolver(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public JWKSource<SecurityContext> resolve(URI jwkSetUri) {
        if (jwkSetUri == null) {
            throw new IllegalArgumentException("JWK Set URI is required");
        }
        String scheme = jwkSetUri.getScheme();
        if (scheme == null || scheme.isBlank()) {
            throw new IllegalArgumentException("JWK Set URI must include a scheme");
        }
        return switch (scheme.toLowerCase(Locale.ROOT)) {
            case "http", "https" -> remoteSource(jwkSetUri);
            case "file", "classpath" -> localSource(jwkSetUri);
            default -> throw new IllegalArgumentException("Unsupported JWK Set URI scheme: " + scheme);
        };
    }

    private JWKSource<SecurityContext> remoteSource(URI jwkSetUri) {
        try {
            return JWKSourceBuilder.<SecurityContext>create(jwkSetUri.toURL()).build();
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("Configured remote JWK Set URI is invalid", exception);
        }
    }

    private JWKSource<SecurityContext> localSource(URI jwkSetUri) {
        Resource resource = resourceLoader.getResource(jwkSetUri.toString());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalStateException("Configured JWK Set resource is not readable");
        }
        try (InputStream inputStream = resource.getInputStream()) {
            return new ImmutableJWKSet<>(JWKSet.load(inputStream));
        } catch (IOException | ParseException exception) {
            throw new IllegalStateException("Configured JWK Set resource cannot be loaded", exception);
        }
    }
}
