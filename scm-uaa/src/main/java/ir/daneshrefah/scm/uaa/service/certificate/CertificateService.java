package ir.daneshrefah.scm.uaa.service.certificate;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-11-18
 */
@Service
public class CertificateService {

    @Getter
    @RequiredArgsConstructor
    public enum EncryptionAlgorithm {
        RSA("RSA", true),
        DSA("DSA", true),
        EC("EC", false),
        DIFFIE_HELLMAN("DiffieHellman", false),
        ED25519("Ed25519", false),
        ED448("Ed448", false);
        
        private final String code;
        private final boolean requireKeySize;
    }

    public Optional<KeyPair> generateKeyPair(EncryptionAlgorithm algorithm, Integer keySize) throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algorithm.code);
        if (algorithm.isRequireKeySize()) {
            keyPairGen.initialize(/*2048*/ keySize, new SecureRandom());
        }
        KeyPair keyPair = keyPairGen.generateKeyPair();
//        PrivateKey privateKey = keyPair.getPrivate();
//        PublicKey publicKey = keyPair.getPublic();
        return Optional.of(keyPair);
    }

    public String exportKeyToPEM(Key key) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(key);
        }
        return stringWriter.toString();
    }

    public String exportCertificateToPEM(X509Certificate certificate) throws IOException {
        StringWriter stringWriter = new StringWriter();
        try (JcaPEMWriter pemWriter = new JcaPEMWriter(stringWriter)) {
            pemWriter.writeObject(certificate);
        }
        return stringWriter.toString();
    }

    public X509Certificate generateSelfSignedCertificate(KeyPair keyPair, String subjectDN, String issuerDN, Integer validityDays) throws Exception {
        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        if (Objects.isNull(validityDays)) {
            validityDays = 365; // 1 year validity
        }
        X500Principal issuer = new X500Principal(issuerDN);
        BigInteger serialNumber = new BigInteger(64, new SecureRandom());
        Date endDate = new Date(now + validityDays * 24 * 60 * 60 * 1000L);

        X500Principal subject = new X500Principal(subjectDN); // in Self-signed, the issuer is also the subject
        PublicKey publicKey = keyPair.getPublic();

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                issuer, serialNumber, startDate, endDate, subject, publicKey);

        ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA").build(keyPair.getPrivate());

        return new JcaX509CertificateConverter().setProvider("BC").getCertificate(certBuilder.build(signer));
    }

}
