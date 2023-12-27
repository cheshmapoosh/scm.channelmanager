package ir.daneshrefah.scm.uaa.controller;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import ir.daneshrefah.scm.uaa.controller.dto.PublicKeyResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
@RestController
public class TokenController extends BaseController {

    private final JWKSet jwkSet;
    private PublicKeyResponse response = null;

    public TokenController(JWKSet jwkSet) {
        this.jwkSet = jwkSet;
        response = generatePublicKey();
    }

    @RequestMapping("/token_key")
    public ResponseEntity<PublicKeyResponse> publicKey() {
        return ResponseEntity.ok(response);
    }

    private PublicKeyResponse generatePublicKey() {
        if (null != response) {
            return response;
        }
        JWK jwk = jwkSet.getKeys().get(0);
        RSAKey rsaKey = (RSAKey) jwk;
        PublicKey publicKey = null;
        try {
            publicKey = rsaKey.toPublicKey();
            String publicKeyPEM = exportPublicKeyToPEM((RSAPublicKey) publicKey);

            response = PublicKeyResponse.builder()
                    .algorithm(jwk.getAlgorithm().getName())
                    .value(publicKeyPEM)
                    .build();
        } catch (JOSEException e) {
            LOGGER.error("error extract public key.", e);
        }
        return response;
    }

    private static String exportPublicKeyToPEM(RSAPublicKey publicKey) {
        byte[] publicKeyBytes = publicKey.getEncoded();
        String base64Encoded = Base64.getEncoder().encodeToString(publicKeyBytes);

        // Format the public key as a PEM string
        StringBuilder pemPublicKey = new StringBuilder();

        // Break the base64-encoded string into lines of 64 characters each
        for (int i = 0; i < base64Encoded.length(); i += 64) {
            pemPublicKey.append(base64Encoded, i, Math.min(i + 64, base64Encoded.length()));
        }
        return pemPublicKey.toString();
    }

}
