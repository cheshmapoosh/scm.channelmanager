package ir.daneshrefah.scm.core.crypto.rsa;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;

public class KeyPairFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String resource;
    private final char[] password;
    private final Object lock = new Object();
    private KeyStore store;

    public KeyPairFactory(String resource, char[] password) {
        this.resource = resource;
        this.password = password;
    }

    public KeyPair getKeyPair(String alias) {
        return getKeyPair(alias, password);
    }

    public KeyPair getKeyPair(String alias, char[] password) {
        InputStream inputStream = null;
        try {
            synchronized (lock) {
                if (store == null) {
                    synchronized (lock) {
                        store = KeyStore.getInstance("jks");
                        inputStream = getInputStream(resource);
                        store.load(inputStream, this.password);
                    }
                }
            }
            RSAPrivateCrtKey key = (RSAPrivateCrtKey) store.getKey(alias, password);
            RSAPublicKeySpec spec = new RSAPublicKeySpec(key.getModulus(), key.getPublicExponent());
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            return new KeyPair(publicKey, key);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load keys from store: " + resource, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.warn("Cannot close open stream: ", e);
            }
        }
    }

    public InputStream getInputStream(String path)   {
        String pathToUse = path.trim();
        if (pathToUse.startsWith("/")) {
            pathToUse = pathToUse.substring(1);
        }
        return getClass().getClassLoader().getResourceAsStream(pathToUse);
    }
}
