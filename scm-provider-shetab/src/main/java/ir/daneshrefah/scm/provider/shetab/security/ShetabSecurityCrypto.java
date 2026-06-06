package ir.daneshrefah.scm.provider.shetab.security;

import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.params.DESParameters;
import org.jpos.iso.ISOUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

public class ShetabSecurityCrypto {
    private static final int DES_KEY_LENGTH_BYTES = 8;
    private static final int DES_BLOCK_LENGTH_BYTES = 8;
    private static final int ISO9797_MAC_LENGTH_BITS = 64;

    public String generatePinBlock(String pin, String pan, String hexKey) {
        validatePin(pin);
        if (pan == null || pan.length() < 13) {
            throw new IllegalArgumentException("PAN must have at least 13 digits to generate ISO-0 PIN block");
        }

        byte[] key = singleDesKey(hexKey, "PIN");
        String pinBlockFirstPart = rightPad(leftPad(Integer.toHexString(pin.length()), 2, '0') + pin, 16, 'f');
        String pinBlockSecondPart = "0000" + pan.substring(pan.length() - 13, pan.length() - 1);

        byte[] first = ISOUtil.hex2byte(pinBlockFirstPart);
        byte[] second = ISOUtil.hex2byte(pinBlockSecondPart);
        byte[] clearPinBlock = new byte[DES_BLOCK_LENGTH_BYTES];
        for (int i = 0; i < DES_BLOCK_LENGTH_BYTES; i++) {
            clearPinBlock[i] = (byte) (first[i] ^ second[i]);
        }
        return ISOUtil.hexString(encryptDes(key, clearPinBlock));
    }

    public String generateIso9797Mac(byte[] message, String hexKey) {
        byte[] key = singleDesKey(hexKey, "MAC");
        DESEngine desEngine = new DESEngine();
        CBCBlockCipherMac mac = new CBCBlockCipherMac(desEngine, ISO9797_MAC_LENGTH_BITS);
        byte[] result = new byte[DES_BLOCK_LENGTH_BYTES];
        mac.init(new DESParameters(key));
        mac.update(message, 0, message.length);
        mac.doFinal(result, 0);
        return ISOUtil.hexString(result);
    }

    private byte[] encryptDes(byte[] key, byte[] clearData) {
        try {
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "DES"));
            return cipher.doFinal(clearData);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not encrypt Shetab PIN block", e);
        }
    }

    private byte[] singleDesKey(String hexKey, String keyName) {
        if (hexKey == null || hexKey.isBlank()) {
            throw new IllegalArgumentException("Shetab " + keyName + " key is required");
        }
        byte[] key = ISOUtil.hex2byte(hexKey);
        if (key.length != DES_KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("Shetab " + keyName + " key must be a single DES key (16 hex characters)");
        }
        return key;
    }

    private void validatePin(String pin) {
        if (pin == null || pin.isBlank()) {
            throw new IllegalArgumentException("PIN is required to generate PIN block");
        }
        if (!pin.matches("\\d{4,12}")) {
            throw new IllegalArgumentException("PIN must be numeric and 4 to 12 digits long");
        }
    }

    private String leftPad(String value, int length, char pad) {
        if (value.length() >= length) {
            return value;
        }
        return String.valueOf(pad).repeat(length - value.length()) + value;
    }

    private String rightPad(String value, int length, char pad) {
        if (value.length() >= length) {
            return value;
        }
        return value + String.valueOf(pad).repeat(length - value.length());
    }
}
