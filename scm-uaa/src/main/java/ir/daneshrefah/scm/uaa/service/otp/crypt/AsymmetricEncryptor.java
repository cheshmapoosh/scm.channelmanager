package ir.daneshrefah.scm.uaa.service.otp.crypt;

import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;

public class AsymmetricEncryptor extends EncryptorImpl {

    private static final Log log = LogFactory.getLog(AsymmetricEncryptor.class);

    public AsymmetricEncryptor(String jcaAlgorithm, String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        super(jcaAlgorithm, provider);
        generatekey();
    }

    public AsymmetricEncryptor(String jcaAlgorithm, String provider, String storeKey, String password, String alias) {
        super(jcaAlgorithm, provider);
        generatekeyfromfile(storeKey, password, StringUtils.isEmpty(alias) ? DEFAULT_ALIAS : alias);
    }

    @Override
    public void generatekey() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(jcaAlgorithm, provider);
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", provider);
        keyGen.initialize(1024, random);
        KeyPair kp = keyGen.generateKeyPair();
        encryptedKey = kp.getPublic();
        decryptedKey = kp.getPrivate();
    }

//    @Override
//    public void generatekeyfromfile(String storekey, String passwordStr, String alias) { //TODO: ask
//        char[] passwordChars = new char[passwordStr.length()];
//        passwordStr.getChars(0, passwordStr.length(), passwordChars, 0);
//        KeyPair keyPair = null;
//        try {
//            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
//            URL url = getClass().getClassLoader().getResource(storekey);
//            if (url != null) {
//                new File(url.getPath());
//            } else {
//                String msg = String.format("not found %s url", storekey);
//                log.error(msg);
//                throw new CryptographyException(msg);
//            }
//            File file = new File(url.getPath());
//            if (!file.exists()) {
//                String msg = String.format("file %s not found . ", storekey);
//                log.error(msg);
//                throw new CryptographyException(msg);
//            }
//            FileInputStream fin = new FileInputStream(file);
//            log.debug(String.format("before load %s", storekey));
//            keyStore.load(fin, passwordChars);
//            log.debug(String.format("after load %s", storekey));
//            Key privateKey = keyStore.getKey(alias, passwordChars);
//            if (privateKey instanceof PrivateKey) {
//                Certificate certificate = keyStore.getCertificate(alias);
//                PublicKey publicKey = certificate.getPublicKey();
//                keyPair = new KeyPair(publicKey, (PrivateKey) privateKey);
//            }
//        } catch (Exception e) {
//            log.error(e);
//        }
//        encryptedKey = keyPair.getPublic();
//        decryptedKey = keyPair.getPrivate();
//    }

    @Override
    public void generatekeyfromfile(String storekey, String passwordStr, String alias) {
        char[] passwordChars = new char[passwordStr.length()];
        passwordStr.getChars(0, passwordStr.length(), passwordChars, 0);
        KeyPair keyPair = null;
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(storekey);
            keyStore.load(resourceAsStream, passwordChars);
            Key privateKey = keyStore.getKey(alias, passwordChars);
            if (privateKey instanceof PrivateKey) {
                Certificate certificate = keyStore.getCertificate(alias);
                PublicKey publicKey = certificate.getPublicKey();
                keyPair = new KeyPair(publicKey, (PrivateKey) privateKey);
            }
        } catch (Exception e) {
            log.error(e);
        }
        assert keyPair != null;
        encryptedKey = keyPair.getPublic();
        decryptedKey = keyPair.getPrivate();
    }
}
