package ir.daneshrefah.scm.uaa.security.password;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scm.uaa.security.password")
public class PasswordEncodingProperties {
    public static final String DEFAULT_CURRENT_ENCODING_ID = "argon2id";
    public static final String DEFAULT_LEGACY_DEFAULT_MATCHING_ID = "legacy-md5";

    private String currentEncodingId = DEFAULT_CURRENT_ENCODING_ID;
    private String legacyDefaultMatchingId = DEFAULT_LEGACY_DEFAULT_MATCHING_ID;

    public String getCurrentEncodingId() {
        return currentEncodingId;
    }

    public void setCurrentEncodingId(String currentEncodingId) {
        this.currentEncodingId = currentEncodingId;
    }

    public String getLegacyDefaultMatchingId() {
        return legacyDefaultMatchingId;
    }

    public void setLegacyDefaultMatchingId(String legacyDefaultMatchingId) {
        this.legacyDefaultMatchingId = legacyDefaultMatchingId;
    }
}
