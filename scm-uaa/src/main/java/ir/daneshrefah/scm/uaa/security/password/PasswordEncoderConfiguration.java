package ir.daneshrefah.scm.uaa.security.password;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(PasswordEncodingProperties.class)
@SuppressWarnings("removal")
public class PasswordEncoderConfiguration {
    static final String LEGACY_MD5_ID = "legacy-md5";
    static final String LEGACY_MD5_COMPATIBILITY_ID = "MD5";

    private static final String ARGON2_ID = "argon2id";
    private static final String ARGON2_COMPATIBILITY_ID = "argon2";
    private static final String ARGON2_ENCODER_CLASS =
            "org.springframework.security.crypto.argon2.Argon2PasswordEncoder";
    private static final String ARGON2_GENERATOR_CLASS =
            "org.bouncycastle.crypto.generators.Argon2BytesGenerator";

    @Bean
    public PasswordEncoder passwordEncoder(PasswordEncodingProperties properties) {
        PasswordEncoder legacyMd5PasswordEncoder = new LegacyUsernameSaltedMd5PasswordEncoder();
        Map<String, PasswordEncoder> encoders = passwordEncoders(legacyMd5PasswordEncoder);
        String currentEncodingId = requireRegisteredEncodingId(
                properties.getCurrentEncodingId(),
                encoders,
                "scm.uaa.security.password.current-encoding-id"
        );
        rejectLegacyCurrentEncodingId(currentEncodingId);
        String legacyDefaultMatchingId = requireRegisteredEncodingId(
                properties.getLegacyDefaultMatchingId(),
                encoders,
                "scm.uaa.security.password.legacy-default-matching-id"
        );

        // The current encoder is used for all new password writes and emits the standard {id} prefix.
        DelegatingPasswordEncoder passwordEncoder = new DelegatingPasswordEncoder(currentEncodingId, encoders);
        // The default matcher keeps old unprefixed username-salted MD5 values readable.
        passwordEncoder.setDefaultPasswordEncoderForMatches(encoders.get(legacyDefaultMatchingId));
        return passwordEncoder;
    }

    @Bean
    public AuthenticationTrustResolver authenticationTrustResolver() {
        return new AuthenticationTrustResolverImpl();
    }

    private Map<String, PasswordEncoder> passwordEncoders(PasswordEncoder legacyMd5PasswordEncoder) {
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put(LEGACY_MD5_ID, legacyMd5PasswordEncoder);
        encoders.put(LEGACY_MD5_COMPATIBILITY_ID, legacyMd5PasswordEncoder);
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2", Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8());
        registerArgon2IfAvailable(encoders);
        return encoders;
    }

    private void rejectLegacyCurrentEncodingId(String currentEncodingId) {
        if (LEGACY_MD5_ID.equals(currentEncodingId) || LEGACY_MD5_COMPATIBILITY_ID.equals(currentEncodingId)) {
            throw new IllegalStateException("Legacy MD5 cannot be configured as the current password encoder");
        }
    }

    private void registerArgon2IfAvailable(Map<String, PasswordEncoder> encoders) {
        ClassLoader classLoader = PasswordEncoderConfiguration.class.getClassLoader();
        if (!ClassUtils.isPresent(ARGON2_ENCODER_CLASS, classLoader)
                || !ClassUtils.isPresent(ARGON2_GENERATOR_CLASS, classLoader)) {
            return;
        }
        try {
            Class<?> argon2PasswordEncoder = ClassUtils.forName(ARGON2_ENCODER_CLASS, classLoader);
            PasswordEncoder encoder = (PasswordEncoder) argon2PasswordEncoder
                    .getMethod("defaultsForSpringSecurity_v5_8")
                    .invoke(null);
            encoders.put(ARGON2_ID, encoder);
            encoders.put(ARGON2_COMPATIBILITY_ID, encoder);
        } catch (ClassNotFoundException e) {
            return;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | LinkageError e) {
            throw new IllegalStateException("Argon2 password encoder is present but could not be initialized", e);
        }
    }

    private String requireRegisteredEncodingId(
            String encodingId,
            Map<String, PasswordEncoder> encoders,
            String propertyName
    ) {
        if (encodingId == null || encodingId.isBlank()) {
            throw new IllegalStateException("Password encoder id is not configured: " + propertyName);
        }
        if (!encoders.containsKey(encodingId)) {
            throw new IllegalStateException("Unsupported password encoder id configured for " + propertyName + ": " + encodingId);
        }
        return encodingId;
    }
}
