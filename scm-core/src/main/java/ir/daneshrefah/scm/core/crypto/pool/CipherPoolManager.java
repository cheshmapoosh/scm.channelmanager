package ir.daneshrefah.scm.core.crypto.pool;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;

public class CipherPoolManager {
    private static final CipherPoolManager CIPHER_POOL_MANAGER = new CipherPoolManager();
    private static GenericObjectPool<Cipher> objectPool;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final CipherPoolFactory cipherPoolFactory = new CipherPoolFactory();

    private CipherPoolManager() {
        configure();
    }

    public static CipherPoolManager getInstance() {
        return CIPHER_POOL_MANAGER;
    }

    private void configure() {
        GenericObjectPoolConfig<Cipher> objectPoolConfig = new GenericObjectPoolConfig<>();
        objectPoolConfig.setMaxTotal(CipherPoolConfig.MAX_POOL_SIZE);
        objectPoolConfig.setMinIdle(CipherPoolConfig.MIN_POOL_SIZE);
        objectPoolConfig.setMaxWaitMillis(CipherPoolConfig.MAX_WAIT_MILLIS);
        objectPool = new GenericObjectPool<>(cipherPoolFactory, objectPoolConfig);
        logger.info(">>> Cipher object pool configured successfully");
    }

    public Cipher borrow() {
        Cipher cipher;
        try {
            cipher = objectPool.borrowObject();
        } catch (Exception e) {
            logger.warn(">>> Cipher object pool could not provide cipher instance , retrying after {}", CipherPoolConfig.THREAD_SLEEP_TIME);
            cipher = retryBorrowObject();
        }
        return cipher;
    }

    private Cipher retryBorrowObject() {
        try {
            Thread.sleep(CipherPoolConfig.THREAD_SLEEP_TIME);
            return objectPool.borrowObject();
        } catch (Exception exception) {
            logger.warn(">>> Cipher object pool retrying for provider failed", exception);
        }
        return null;
    }

    public void giveBack(Cipher cipher) {
        logger.info("Giving back instance");
        objectPool.returnObject(cipher);
    }
}
