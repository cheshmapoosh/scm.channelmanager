package ir.daneshrefah.scm.core.crypto.rsa;


import java.security.KeyPair;

public class KayPairProvider {
    private static final KayPairProvider KAY_PAIR_PROVIDER = new KayPairProvider();
    private static KeyPair keyPair;

    private KayPairProvider() {
        init();
    }

    public static KayPairProvider getInstance() {
        return KAY_PAIR_PROVIDER;
    }

    public KeyPair getKeyPair() {
        return keyPair;
    }

    private void init() {
        keyPair = new KeyPairFactory("keystore-cipher.p12",
                KeyStoreInfo.password.toCharArray()).getKeyPair(KeyStoreInfo.alias);

    }
}
