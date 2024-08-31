package ir.daneshrefah.scm.uaa.server.security.crypto.password;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5UsernamePasswordEncoder implements PasswordEncoder, UsernamePasswordEncoder {
    private final static String BASE_SALT = "K4a07WBd3qI+mdEg1ZboFRyBdktIfsGL6pJ7Xlpvr3GxKaZIrB9KBaHs+E8Ma2ZxBM9VezLA4Hk4lj1Kgq1XPg==";
    private final MessageDigest digester;

    public Md5UsernamePasswordEncoder() {
        this.digester = createDigest();
    }

    @Override
    public String encode(CharSequence rawPassword) {
        throw new UnsupportedOperationException("Username must be provided for encoding");
    }
    @Override
    public String encode(CharSequence rawPassword, String username) {
        String salt = generateSalt(username);
        String saltedPassword = rawPassword + salt;
        return digest(salt, saltedPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        throw new UnsupportedOperationException("Username must be provided for matching");
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword, String username) {
        String salt = generateSalt(username);
        String rawPasswordEncoded = digest(salt, rawPassword);
        return StringUtils.equals(rawPasswordEncoded, encodedPassword);
    }

    private String digest(String salt, CharSequence rawPassword) {
        String saltedPassword = rawPassword + salt;
        byte[] digest = this.digester.digest(Utf8.encode(saltedPassword));
        return encode(digest);
    }

    private String encode(byte[] digest) {
        return new String(Hex.encode(digest));
    }

    private String generateSalt(String username) {
        return "{" + BASE_SALT + username + "}";
    }

    private static MessageDigest createDigest() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("No such hashing algorithm", ex);
        }
    }

    public static void main(String[] args) {
        Md5UsernamePasswordEncoder encoder = new Md5UsernamePasswordEncoder();
        System.out.println(encoder.encode("client-secret", "client-id"));
    }
}
