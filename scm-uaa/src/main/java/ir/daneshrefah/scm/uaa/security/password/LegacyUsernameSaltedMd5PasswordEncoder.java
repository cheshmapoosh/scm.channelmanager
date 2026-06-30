package ir.daneshrefah.scm.uaa.security.password;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Legacy SCM password encoder kept only for records stored with the old username-salted MD5 format.
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public class LegacyUsernameSaltedMd5PasswordEncoder implements PasswordEncoder {
    private static final String SALT = "K4a07WBd3qI+mdEg1ZboFRyBdktIfsGL6pJ7Xlpvr3GxKaZIrB9KBaHs+E8Ma2ZxBM9VezLA4Hk4lj1Kgq1XPg==";

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword instanceof LegacyPassword legacyPassword) {
            return digest(mergePasswordAndSalt(legacyPassword.password(), legacyPassword.username()));
        }
        return digest(rawPassword == null ? "" : rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null) {
            return false;
        }
        return encodedPassword.trim().equalsIgnoreCase(encode(rawPassword));
    }

    private String mergePasswordAndSalt(CharSequence password, String username) {
        return password + "{" + SALT + (username == null ? "" : username) + "}";
    }

    private String digest(String value) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
            return new String(Hex.encode(digest));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 digest is not available", e);
        }
    }
}
