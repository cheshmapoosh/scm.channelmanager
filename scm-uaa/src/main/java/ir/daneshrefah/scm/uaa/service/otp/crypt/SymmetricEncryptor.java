package ir.daneshrefah.scm.uaa.service.otp.crypt;

import ir.daneshrefah.scm.utils.string.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.security.*;

public class SymmetricEncryptor extends EncryptorImpl {

    public SymmetricEncryptor(String jcaAlgorithm, String provider) throws NoSuchAlgorithmException {
        super(jcaAlgorithm, provider);
        generatekey();
    }

    public SymmetricEncryptor(String jcaAlgorithm, String provider, String storeKey, String password, String alias) {
        super(jcaAlgorithm, provider);
        generatekeyfromfile(storeKey, password, StringUtils.isEmpty(alias) ? DEFAULT_ALIAS : alias);
    }

    public SymmetricEncryptor(String jcaAlgorithm, String provider, byte[] keybyte) {
        super(jcaAlgorithm, provider);
        decryptedKey = new ModeSecretKeySpec(keybyte, jcaAlgorithm, Cipher.DECRYPT_MODE);
        encryptedKey = new ModeSecretKeySpec(keybyte, jcaAlgorithm, Cipher.ENCRYPT_MODE);
    }

    public void generatekey() throws NoSuchAlgorithmException {
        KeyGenerator kgen = KeyGenerator.getInstance(jcaAlgorithm);
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        decryptedKey = new ModeSecretKeySpec(raw, jcaAlgorithm, Cipher.DECRYPT_MODE);
        encryptedKey = new ModeSecretKeySpec(raw, jcaAlgorithm, Cipher.ENCRYPT_MODE);
    }

    @Override
    public void generatekeyfromfile(String storekey, String passwordStr, String alias) {
        char[] passwordChars = new char[passwordStr.length()];
        passwordStr.getChars(0, passwordStr.length(), passwordChars, 0);
        KeyPair keyPair = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fin = new FileInputStream(new File(storekey));
            keyStore.load(fin, passwordChars);
            Key privateKey = keyStore.getKey(alias, passwordChars);
            if (privateKey instanceof PrivateKey) {
                java.security.cert.Certificate certificate = keyStore.getCertificate(alias);
                PublicKey publicKey = certificate.getPublicKey();
                keyPair = new KeyPair(publicKey, (PrivateKey) privateKey);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        encryptedKey = keyPair.getPublic();
        decryptedKey = keyPair.getPrivate();
    }

    /**
     * The Class ModeSecretKeySpec.
     */
    class ModeSecretKeySpec extends SecretKeySpec {


        /**
         * The mode.
         */
        private int mode;

        /**
         * Gets the mode.
         *
         * @return the mode
         */
        public int getMode() {
            return mode;
        }

        /**
         * Sets the mode.
         *
         * @param mode the new mode
         */
        public void setMode(int mode) {
            this.mode = mode;
        }

        /**
         * Instantiates a new mode secret key spec.
         *
         * @param bytes the bytes
         * @param s     the s
         */
        ModeSecretKeySpec(byte[] bytes, String s) {
            super(bytes, s);
        }

        /**
         * Instantiates a new mode secret key spec.
         *
         * @param bytes the bytes
         * @param i     the i
         * @param i1    the i1
         * @param s     the s
         */
        ModeSecretKeySpec(byte[] bytes, int i, int i1, String s) {
            super(bytes, i, i1, s);
        }

        /**
         * Instantiates a new mode secret key spec.
         *
         * @param bytes the bytes
         * @param s     the s
         * @param mode  the mode
         */
        ModeSecretKeySpec(byte[] bytes, String s, int mode) {
            super(bytes, s);
            this.mode = mode;
        }

        /* (non-Javadoc)
         * @see javax.crypto.spec.SecretKeySpec#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ModeSecretKeySpec)) return false;
            if (!super.equals(o)) return false;

            ModeSecretKeySpec that = (ModeSecretKeySpec) o;

            if (mode != that.mode) return false;

            return true;
        }

        /* (non-Javadoc)
         * @see javax.crypto.spec.SecretKeySpec#hashCode()
         */
        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + mode;
            return result;
        }
    }
}
