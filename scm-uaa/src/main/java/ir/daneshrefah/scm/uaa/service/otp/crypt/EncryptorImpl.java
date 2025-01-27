package ir.daneshrefah.scm.uaa.service.otp.crypt;

import ir.daneshrefah.scm.uaa.exception.otp.CryptographyException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.*;

import static javax.crypto.Cipher.ENCRYPT_MODE;

// TODO: Auto-generated Javadoc

public abstract class EncryptorImpl implements Encryptor {

    /**
     * The Constant DEFAULT_JCA_ALGORITHM.
     */
    public static final String DEFAULT_JCA_ALGORITHM = "Blowfish";

    /**
     * The Constant DEFAULT_PROVIDER.
     */
    public static final String DEFAULT_PROVIDER = "IBMJCE";


    public static final String DEFAULT_ALIAS = "alias";

    /**
     * The provider.
     */
    protected String provider;

    /**
     * The jca algorithm.
     */
    protected String jcaAlgorithm;

    /**
     * The key bytes.
     */
    private byte[] keyBytes;

    /**
     * The encrypted key
     */
    protected Key encryptedKey;

    /**
     * The encrypted key
     */
    protected Key decryptedKey;

    /**
     * The cipher pool.
     */
    private Map<Key, Cipher> cipherPool = new HashMap<Key, Cipher>();

    /**
     * The instance pool.
     */
    private static Map<Encryptor, Encryptor> instancePool = new HashMap<Encryptor, Encryptor>();

    private static List<String> asymmetricAlgorithmsList = Arrays.<String>asList(new String[]{"DSA", "RSA", "ElGamal", "Diffie-Hellman", "NTRUEncrypt", "PES"});

    private static List<String> symmetricAlgorithmsList = Arrays.<String>asList(new String[]{"AES", "DES", "Blowfish", "IDEA", "RC4", "PES", "Rijndeal", "Twofish", "Square", "Serpent", "SkipJack", "DESede", "CAST5"});

    /**
     * Instantiates a new encryptor impl.
     */
    public EncryptorImpl() {
    }

    /**
     * Instantiates a new encryptor impl.
     *
     * @param jcaAlgorithm the jca algorithm
     */
    protected EncryptorImpl(String jcaAlgorithm, String provider) {
        this.jcaAlgorithm = jcaAlgorithm;
        this.provider = provider;
    }


    /**
     * Gets the single instance of EncryptorImpl.
     *
     * @return single instance of EncryptorImpl
     */
    public static Encryptor getInstance() {
        return getInstance(DEFAULT_JCA_ALGORITHM, DEFAULT_PROVIDER);
    }

    /**
     * Gets the single instance of EncryptorImpl.
     *
     * @param jcaAlgorithm the jca algorithm
     * @return single instance of EncryptorImpl
     */

    public static Encryptor getInstance(String jcaAlgorithm) {
        return getInstance(jcaAlgorithm, DEFAULT_PROVIDER);
    }

    /**
     * Gets the single instance of EncryptorImpl.
     *
     * @param jcaAlgorithm the jca algorithm
     * @param provider     the  provider
     * @return single instance of EncryptorImpl
     */

    public static Encryptor getInstance(String jcaAlgorithm, String provider) {
        try {
            return getInstance(issymmetric(jcaAlgorithm) ? new SymmetricEncryptor(jcaAlgorithm, provider) : new AsymmetricEncryptor(jcaAlgorithm, provider));
        } catch (Exception e) {
            throw new CryptographyException("cryptography Exception", e);
        }
    }

    /**
     * Gets the single instance of EncryptorImpl.
     *
     * @param storeKey url of storekey file
     * @param password the  password of storekey
     * @param alias
     * @return single instance of EncryptorImpl
     */

    public static Encryptor getInstance(String storeKey, String password, String alias) {
        return getInstance(DEFAULT_JCA_ALGORITHM, DEFAULT_PROVIDER, storeKey, password, alias);
    }

    /**
     * Gets the single instance of EncryptorImpl.
     *
     * @param jcaAlgorithm the jca algorithm
     * @param storeKey     url of storekey file
     * @param password     the  password of storekey
     * @param alias
     * @return single instance of EncryptorImpl
     */

    public static Encryptor getInstance(String jcaAlgorithm, String storeKey, String password, String alias) {
        return getInstance(jcaAlgorithm, DEFAULT_PROVIDER, storeKey, password, alias);
    }

    /**
     * Gets the single instance of EncryptorImpl.
     *
     * @param jcaAlgorithm the jca algorithm
     * @param provider     the  provider
     * @param storeKey     url of storekey file
     * @param password     the  password of storekey
     * @param alias
     * @return single instance of EncryptorImpl
     */
    public static Encryptor getInstance(String jcaAlgorithm, String provider, String storeKey, String password, String alias) {
        try {
            return getInstance(issymmetric(jcaAlgorithm) ? new SymmetricEncryptor(jcaAlgorithm, provider, storeKey, password, alias) : new AsymmetricEncryptor(jcaAlgorithm, provider, storeKey, password, alias));
        } catch (Exception e) {
            throw new CryptographyException("cryptography Exception", e);
        }
    }

    public static Encryptor getInstance(byte[] key) {
        return getInstance(DEFAULT_JCA_ALGORITHM, DEFAULT_PROVIDER, key);
    }

    public static Encryptor getInstance(String jcaAlgorithm, byte[] key) {
        return getInstance(jcaAlgorithm, DEFAULT_PROVIDER, key);
    }

    public static Encryptor getInstance(String jcaAlgorithm, String provider, byte[] key) {
        try {
            if (issymmetric(jcaAlgorithm)) {
                return getInstance(new SymmetricEncryptor(jcaAlgorithm, provider, key));
            } else {
                throw new CryptographyException("Symmetric Algorithm be needed");
            }
        } catch (Exception e) {
            throw new CryptographyException("cryptography Exception", e);
        }
    }


    /**
     * Gets the single instance of EncryptorImpl.
     *
     * @param encryptor the encryptor
     * @return single instance of EncryptorImpl
     */
    private static Encryptor getInstance(Encryptor encryptor) {
        if (!instancePool.containsKey(encryptor)) {
            instancePool.put(encryptor, encryptor);
        }
        return instancePool.get(encryptor);
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setJcaAlgorithm(String jcaAlgorithm) {
        this.jcaAlgorithm = jcaAlgorithm;
    }

    public void setEncryptedKey(Key encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public void setDecryptedKey(Key decryptedKey) {
        this.decryptedKey = decryptedKey;
    }

    public Key getEncryptedKey() {
        return encryptedKey;
    }

    public Key getDecryptedKey() {
        return decryptedKey;
    }

    public abstract void generatekey() throws NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException;

    public abstract void generatekeyfromfile(String storekey, String password, String alias) throws NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException, InvalidKeyException;

    public static boolean issymmetric(String jcaAlgorithm) throws NoSuchAlgorithmException {
        if (!symmetricAlgorithmsList.contains(jcaAlgorithm) && !asymmetricAlgorithmsList.contains(jcaAlgorithm)) {
            throw new NoSuchAlgorithmException("invalid algorithm");
        }
        return symmetricAlgorithmsList.contains(jcaAlgorithm) ? true : false;

    }

    public String decrypt(byte[] data) throws UnsupportedEncodingException {
        return new String(decrypt(data, Boolean.FALSE), "CP1256");
    }

    public byte[] encrypt(byte[] data, boolean encoding) {
        try {
            Cipher cipher = getCipher(ENCRYPT_MODE);
            byte[] encrypted = cipher.doFinal(data);
            if (encoding) {
                return Base64.getEncoder().encode(encrypted);
            }
            return encrypted;
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    public byte[] encrypt(String data) throws UnsupportedEncodingException {
        return this.encrypt(data.getBytes("CP1256"), Boolean.FALSE);
    }

    /**
     * Handle exception.
     *
     * @param e the e
     * @throws CryptographyException the cryptography exception
     */
    private void handleException(Exception e) throws CryptographyException {
        throw new CryptographyException(e.getMessage(), e);
    }

    /**
     * Gets the cipher.
     *
     * @param mode the mode
     * @return the cipher
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws NoSuchProviderException  the no such provider exception
     * @throws NoSuchPaddingException   the no such padding exception
     * @throws InvalidKeyException      the invalid key exception
     */
    private Cipher getCipher(int mode) throws NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException {
        if (!cipherPool.containsKey(createKeySpec(mode))) {
            Cipher cipher = Cipher.getInstance(jcaAlgorithm);
            cipher.init(mode, createKeySpec(mode));
            cipherPool.put(createKeySpec(mode), cipher);
        }
        return cipherPool.get(createKeySpec(mode));
    }

    /* (non-Javadoc)
     * @see ir.dpi.cm.security.common.crypt.Encryptor#decrypt(byte[])
     */
    public byte[] decrypt(byte[] data, boolean encoding) {
        try {
            byte[] cipherText = data;
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            if (encoding) {
                cipherText = Base64.getDecoder().decode(new String(data, StandardCharsets.UTF_8));
            }
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            throw new CryptographyException(e.getMessage(), e);
        }
    }

    /**
     * Creates the key spec.
     *
     * @param mode the mode
     * @return the key
     */
    private Key createKeySpec(int mode) {
        return mode == Cipher.ENCRYPT_MODE ? encryptedKey : decryptedKey;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EncryptorImpl)) return false;

        EncryptorImpl encryptor = (EncryptorImpl) o;

        if (jcaAlgorithm != null ? !jcaAlgorithm.equals(encryptor.jcaAlgorithm) : encryptor.jcaAlgorithm != null)
            return false;
        if (encryptor.encryptedKey != encryptedKey || encryptor.decryptedKey != decryptedKey) return false;
        if (provider != null ? !provider.equals(encryptor.provider) : encryptor.provider != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = provider != null ? provider.hashCode() : 0;
        result = 31 * result + (jcaAlgorithm != null ? jcaAlgorithm.hashCode() : 0);
        result = 31 * result + (encryptedKey != null ? encryptedKey.hashCode() : 0);
        result = 31 * result + (decryptedKey != null ? decryptedKey.hashCode() : 0);
        return result;
    }

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
