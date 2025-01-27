package ir.daneshrefah.scm.uaa.service.otp.crypt;

import java.io.UnsupportedEncodingException;

public interface Encryptor {

    byte[] encrypt(byte[] data, boolean encoding);

    byte[] decrypt(byte[] data, boolean encoding);

    byte[] encrypt(String data) throws UnsupportedEncodingException;

    String decrypt(byte[] data) throws UnsupportedEncodingException;
}