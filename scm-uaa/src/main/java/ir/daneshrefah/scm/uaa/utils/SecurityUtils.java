package ir.daneshrefah.scm.uaa.utils;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityUtils {

    private final JWKSet jwkSet;
    private PrivateKey privateKey = null;
    private static final Lock lock = new ReentrantLock();

    @Getter
    private static SecurityUtils instance;

    @PostConstruct
    public void init() {
        instance = this;
        initPrivateKey();
        log.info("SecurityUtils has been initialized");
    }

    @SneakyThrows
    private void initPrivateKey() {
        if (Objects.isNull(privateKey)) {
            try {
                if (lock.tryLock()) {
                    JWK jwk = jwkSet.getKeys().get(0);
                    RSAKey rsaKey = (RSAKey) jwk;
                    privateKey = rsaKey.toPrivateKey();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    public String decryptPassword(String encryptedPassword) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedPassword);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    @PreDestroy
    public void destroy() {
        this.privateKey = null;
        SecurityUtils.instance = null;
        log.info("SecurityUtils has been destroyed");
    }

}
