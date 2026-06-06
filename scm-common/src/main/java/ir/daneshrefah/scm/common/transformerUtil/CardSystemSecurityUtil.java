package ir.daneshrefah.scm.common.transformerUtil;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.jpos.iso.ISOUtil;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jpos.security.SMException;
import org.jpos.security.jceadapter.JCEHandlerException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.simple.JSONObject;

@Slf4j
public class CardSystemSecurityUtil {

    private static final String ALG_DES = "DES";
    private static final String ALG_TRIPLE_DES = "DESede";
    private static final String DES_MODE = "ECB";
    private static final String DES_PADDING = "NoPadding";
    private static Provider provider;
    private static final int SHORT_DES_LENGTH = 64;
    private static final int DOUBLE_DES_LENGTH = 128;
    private static final int TRIPLE_DES_LENGTH = 192;
    private static String pinKey = "1C1C1C1C1C1C1C1C";//////////////??????????????????

    public static String encryptPin(String pin, String cardNo) {
        return genPIN(pin, cardNo, pinKey);
    }

    public static String genPIN(String PIN, String CardNo, String encryptionKey) {
        try {
            String pinBlockFirstPart = ISOUtil.padright((new StringBuilder(String.valueOf(
                    ISOUtil.padleft(Integer.toHexString(PIN.length()), 2, '0')))).append(PIN).toString(), 16, 'f');
            String pinBlockSecondPart = (new StringBuilder("0000")).append(
                    CardNo.substring(CardNo.length() - 13, CardNo.length() - 1)).toString();
            byte firstPartXORSecondPart[] = new byte[8];
            byte firstPartHex[] = ISOUtil.hex2byte(pinBlockFirstPart);
            byte secondPartHex[] = ISOUtil.hex2byte(pinBlockSecondPart);
            for(int i = 0; i < 8; i++)
            {
                firstPartXORSecondPart[i] = (byte)(firstPartHex[i] ^ secondPartHex[i]);
            }

            byte clearPinBlock[] = encryptByClearKey(ISOUtil.hex2byte(encryptionKey), firstPartXORSecondPart);
            return ISOUtil.hexString(clearPinBlock);
        }
        catch(Exception exception) {
            return "";
        }
    }

    private static byte[] encryptByClearKey(byte desKey[], byte clearData[]) throws SMException {
        Key skey = formDESKey((short) SHORT_DES_LENGTH, desKey);
        return encryptData(clearData, skey);
    }

    private static Key formDESKey(short keyLength, byte clearKeyBytes[]) throws JCEHandlerException {
        Key key = null;
        switch(keyLength){
            case SHORT_DES_LENGTH :
                key = new SecretKeySpec(clearKeyBytes, ALG_DES);
                break;
            case DOUBLE_DES_LENGTH :
                clearKeyBytes = ISOUtil.concat(clearKeyBytes, 0, getBytesLength((short)DOUBLE_DES_LENGTH),
                        clearKeyBytes, 0, getBytesLength((short) SHORT_DES_LENGTH));
            case TRIPLE_DES_LENGTH :
                key = new SecretKeySpec(clearKeyBytes, ALG_TRIPLE_DES);
                break;
        }
        if(key == null) {
            throw new JCEHandlerException((new StringBuilder("Unsupported DES key length: ")).append(keyLength).append(" bits").toString());
        }
        else {
            return key;
        }
    }

    private static byte[] encryptData(byte data[], Key key) throws JCEHandlerException {
        return doCryptStuff(data, key, Cipher.ENCRYPT_MODE);
    }

    private static byte[] doCryptStuff(byte data[], Key key, int CipherMode) throws JCEHandlerException {
        String transformation;
        if(key.getAlgorithm().startsWith(ALG_DES)) {
            transformation = (new StringBuilder(String.valueOf(key.getAlgorithm()))).append("/").
                    append(DES_MODE).append("/").append(DES_PADDING).toString();
        }
        else {
            transformation = key.getAlgorithm();
        }
        byte result[];
        try {
            if(provider == null) {
                provider = new BouncyCastleProvider();
            }
            Cipher c1 = Cipher.getInstance(transformation, provider);
            c1.init(CipherMode, key);
            result = c1.doFinal(data);
        }
        catch(Exception e) {
            throw new JCEHandlerException(e);
        }
        return result;
    }

    private static int getBytesLength(short keyLength) throws JCEHandlerException {
        int bytesLength = 0;
        switch(keyLength)
        {
            case SHORT_DES_LENGTH :
                bytesLength = 8;
                break;
            case DOUBLE_DES_LENGTH :
                bytesLength = 16;
                break;
            case TRIPLE_DES_LENGTH :
                bytesLength = 24;
                break;
            default:
                throw new JCEHandlerException((new StringBuilder("Unsupported key length: ")).
                        append(keyLength).append(" bits").toString());
        }
        return bytesLength;
    }

    public static JSONObject decrypt(JSONObject params) {
        Map<String, String> map = new HashMap<>();
        Cipher borrowed = null;
        try {
            System.out.println(CipherPoolManager.class.getClassLoader());
            System.out.println(CipherPoolManager.getInstance());
            borrowed = CipherPoolManager.getInstance().borrow();
            for (Object key : params.keySet()) {
                String s = String.valueOf(params.get(key));
                byte[] decode = Base64.decode(s.getBytes());
                byte[] bytes = borrowed.doFinal(decode);
                String b = new String(bytes);
                map.put(String.valueOf(key), b);
            }
        } catch (Exception e) {
            log.error("could not decrypt params ", e);
        } finally {
            if (Objects.nonNull(borrowed)) {
                CipherPoolManager.getInstance().giveBack(borrowed);
            }
        }
        return new JSONObject(map);
    }


}
