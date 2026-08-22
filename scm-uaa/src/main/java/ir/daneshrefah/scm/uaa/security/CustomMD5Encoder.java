package ir.daneshrefah.scm.uaa.security;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class CustomMD5Encoder {
    private final static String SALT="K4a07WBd3qI+mdEg1ZboFRyBdktIfsGL6pJ7Xlpvr3GxKaZIrB9KBaHs+E8Ma2ZxBM9VezLA4Hk4lj1Kgq1XPg==";

    public String encodePassword(String rawPass, String username){
        String saltedPass = mergePasswordAndSalt(rawPass,username);
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest;
        digest = messageDigest.digest(saltedPass.getBytes(StandardCharsets.UTF_8));
        return  new String(Hex.encode(digest));
    }


    private String mergePasswordAndSalt(String password, String realUsername) {
        return password + "{" + SALT+realUsername + "}";
    }
}
