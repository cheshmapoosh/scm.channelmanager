package ir.daneshrefah.scm.uaa.security.password;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PasswordHashService {
    private static final Set<String> LEGACY_ENCODING_IDS = Set.of(
            PasswordEncoderConfiguration.LEGACY_MD5_ID,
            PasswordEncoderConfiguration.LEGACY_MD5_COMPATIBILITY_ID
    );

    private final PasswordEncoder passwordEncoder;
    private final PasswordEncodingProperties properties;

    public PasswordHashService(
            PasswordEncoder passwordEncoder,
            PasswordEncodingProperties properties
    ) {
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    public String encodeNewPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public boolean matchesLoginPassword(String rawPassword, String username, String storedPassword) {
        return matchesPassword(rawPassword, username, storedPassword);
    }

    public boolean matchesTransactionPassword(String rawPassword, String username, String storedPassword) {
        return matchesPassword(rawPassword, username, storedPassword);
    }

    public boolean upgradeEncodingRequired(String storedPassword) {
        String encodingId = extractEncodingId(storedPassword);
        return encodingId == null || !properties.getCurrentEncodingId().equals(encodingId);
    }

    private boolean matchesPassword(String rawPassword, String username, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        CharSequence password = requiresLegacyUsernameSalt(storedPassword)
                ? new LegacyPassword(rawPassword, username)
                : rawPassword;
        try {
            return passwordEncoder.matches(password, storedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean requiresLegacyUsernameSalt(String storedPassword) {
        String encodingId = extractEncodingId(storedPassword);
        return encodingId == null || LEGACY_ENCODING_IDS.contains(encodingId);
    }

    private String extractEncodingId(String storedPassword) {
        if (storedPassword == null || !storedPassword.startsWith("{")) {
            return null;
        }
        int end = storedPassword.indexOf('}');
        if (end <= 1) {
            return null;
        }
        return storedPassword.substring(1, end);
    }
}
