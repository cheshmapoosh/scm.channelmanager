package ir.daneshrefah.scm.uaa.security.password;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("removal")
class LegacyPasswordTest {

    @Test
    void toStringDoesNotExposeRawPassword() {
        LegacyPassword password = new LegacyPassword("raw-secret", "legacy-user");

        assertEquals("[PROTECTED]", password.toString());
        assertFalse(password.toString().contains("raw-secret"));
    }

    @Test
    void passwordEncoderMatchesLegacyUsernameSaltedHash() {
        PasswordEncoder passwordEncoder = new LegacyUsernameSaltedMd5PasswordEncoder();
        LegacyPassword password = new LegacyPassword("secret", "legacy-user");

        assertTrue(passwordEncoder.matches(password, "bd9d9dd04ccda5ba8beef69569aacd7a"));
        assertFalse(passwordEncoder.matches(new LegacyPassword("wrong", "legacy-user"),
                "bd9d9dd04ccda5ba8beef69569aacd7a"));
    }
}
