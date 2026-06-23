package ir.daneshrefah.scm.core.services.crypto;



import ir.daneshrefah.scm.core.crypto.pool.CipherPoolManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
public class DefaultSensitiveDataDecryptService implements SensitiveDataDecryptService {

    private final CipherPoolManager cipherPoolManager = CipherPoolManager.getInstance();

    @Override
    public String decrypt(String encryptedValue) {
        if (StringUtils.isBlank(encryptedValue)) {
            return encryptedValue;
        }


        if (isProbablyPlainSensitiveValue(encryptedValue)) {
            return encryptedValue;
        }

        Cipher cipher = null;

        try {
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedValue);
            cipher = cipherPoolManager.borrow();

            if (cipher == null) {
                throw new IllegalStateException("CipherPoolManager returned null cipher");
            }

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8).trim();

        } catch (IllegalArgumentException e) {
            /*
             * Base64 decode failed
             */
            throw new IllegalArgumentException("Encrypted value is not valid Base64", e);

        } catch (Exception e) {
            throw new IllegalStateException("Could not decrypt sensitive value", e);

        } finally {
            if (cipher != null) {
                try {
                    cipherPoolManager.giveBack(cipher);
                } catch (Exception e) {
                    log.warn("Could not return cipher to pool", e);
                }
            }
        }
    }

    private boolean isProbablyPlainSensitiveValue(String value) {
        return value.matches("\\d{3,12}");
    }
}
