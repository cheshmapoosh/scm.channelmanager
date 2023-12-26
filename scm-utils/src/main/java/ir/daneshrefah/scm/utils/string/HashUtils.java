package ir.daneshrefah.scm.utils.string;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-26
 */
public class HashUtils {

    private static final Map<String, MessageDigest> ALGORITHMS = new ConcurrentHashMap<>();

    static {
        try {
            // Use computeIfAbsent() for thread-safe initialization
            ALGORITHMS.computeIfAbsent("MD5", algorithm -> {
                try {
                    return MessageDigest.getInstance(algorithm);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e); // Handle exception appropriately
                }
            });
            ALGORITHMS.computeIfAbsent("SHA-1", algorithm -> {
                try {
                    return MessageDigest.getInstance(algorithm);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e); // Handle exception appropriately
                }
            });
            ALGORITHMS.computeIfAbsent("SHA-256", algorithm -> {
                try {
                    return MessageDigest.getInstance(algorithm);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e); // Handle exception appropriately
                }
            });
            // Add more algorithms as needed
        } catch (RuntimeException e) {
            // Handle exception if algorithm initialization fails
            throw e;
        }
    }

    public static byte[] hash(String algorithm, String input) {
        MessageDigest digest = ALGORITHMS.get(algorithm);
        if (digest == null) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }
        return digest.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String hashMD5ToString(String input) {
        return hashToString("MD5", input);
    }

    public static String hashToString(String algorithm, String input) {
        byte[] hashBytes = hash(algorithm, input);
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
