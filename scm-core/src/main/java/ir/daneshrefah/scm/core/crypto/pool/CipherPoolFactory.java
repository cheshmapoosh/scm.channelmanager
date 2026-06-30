package ir.daneshrefah.scm.core.crypto.pool;


import ir.daneshrefah.scm.core.crypto.rsa.KayPairProvider;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.KeyPair;

public class CipherPoolFactory extends BasePooledObjectFactory<Cipher> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KeyPair keyPair = KayPairProvider.getInstance().getKeyPair();


    @Override
    public Cipher create() throws Exception {
        return createInstance();
    }

    @Override
    public PooledObject<Cipher> wrap(Cipher cipher) {
        return new DefaultPooledObject<>(cipher);
    }

    private Cipher createInstance() {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
        } catch (Exception e) {
            logger.error(">>> Cipher pool factory could not create cipher instance.", e);
        }
        return cipher;
    }

}
