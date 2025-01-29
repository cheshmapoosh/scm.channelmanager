package ir.daneshrefah.scm.uaa.service.otp.generator;

import ir.daneshrefah.scm.uaa.domain.otp.*;
import ir.daneshrefah.scm.uaa.exception.OtpServiceException;
import ir.daneshrefah.scm.uaa.exception.otp.CryptographyException;
import ir.daneshrefah.scm.uaa.service.otp.crypt.Encryptor;
import ir.daneshrefah.scm.uaa.service.otp.crypt.EncryptorImpl;
import ir.daneshrefah.scm.uaa.service.otp.crypt.model.OtpChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public abstract class AbstractOtpMessageGenerator implements OtpMessageGenerator {

    private static final String SEPARATING_TOKEN = "#";
    private static final int MIN_SIZE_OF_LIST_RESPONSE = 3;


    public OtpMessage generateRequest(OtpMessageModel otpMessage) {
        return new OtpMessage(otpMessage.getOtpChannelId(), new RequestMessageDetails(otpMessage.getRequestNo(), getMessageType(), createRequestBody(otpMessage)));
    }

    public byte[] transformRequest(OtpMessage otpMessage, OtpChannel otpChannel) {
        List<String> listMessage = otpMessage.toList();
        String nonEncryptedPartOfMessage = getNonEncryptedPartOfMessage(listMessage);
        byte[] encryptedPartOfMessage = encryptRequest(getEncryptedPartOfMessage(listMessage), otpChannel);
        byte[] requestMessage = new byte[nonEncryptedPartOfMessage.length() + encryptedPartOfMessage.length];
        System.arraycopy(nonEncryptedPartOfMessage.getBytes(), 0, requestMessage, 0, nonEncryptedPartOfMessage.length());
        System.arraycopy(encryptedPartOfMessage, 0, requestMessage, nonEncryptedPartOfMessage.length(), encryptedPartOfMessage.length);
        return requestMessage;
    }

    public Object generateResponse(byte[] response, OtpChannel otpChannel) {
        String textResponse = decryptResponse(response, otpChannel);
        List<String> textMessageAsList = convertTextMessageToList(textResponse);
        textMessageAsList.add(0, String.valueOf(otpChannel.getId()));
        if (textMessageAsList.size() < MIN_SIZE_OF_LIST_RESPONSE) {
            throw new OtpServiceException();
        } else {
            OtpMessage otpResponse = new OtpMessage(new ResponseMessageDetails(createResponseBody()));
            otpResponse.setFieldsFromList(textMessageAsList);
            return otpResponse.getMessageDetails();
        }
    }

    public List<String> convertTextMessageToList(String textMessage) {
        List<String> textMessageAsList = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(textMessage, SEPARATING_TOKEN);
        while (st.hasMoreTokens()) {
            textMessageAsList.add(st.nextToken());
        }
        return textMessageAsList;
    }

    private byte[] encryptRequest(String message, OtpChannel otpChannel) {
        byte[] cipherText;
        try {

            cipherText = getEncryptor(otpChannel).encrypt(message);
        } catch (Exception e) {
            throw new CryptographyException("Exception occurred during encryption", e);
        }
        return cipherText;
    }

    private String decryptResponse(byte[] message, OtpChannel otpChannel) {
        String clearText;
        try {
            clearText = getEncryptor(otpChannel).decrypt(message);
        } catch (Exception e) {
            throw new CryptographyException("Exception occurred during decryption", e);
        }
        return clearText;
    }

    private String arrangeMessage(List<String> listOfFieldsOfMessage) {

        StringBuilder sb = new StringBuilder();
        for (String field : listOfFieldsOfMessage) {
            sb.append(field);
            sb.append(SEPARATING_TOKEN);
        }
        return sb.toString();
    }

    private Encryptor getEncryptor(OtpChannel otpChannel) {
        Encryptor encryptor;
        if (otpChannel.getKeyBytes() == null) {
            encryptor = EncryptorImpl.getInstance(otpChannel.getJcaAlgorithm(), otpChannel.getProvider(), otpChannel.getStoreKey(), otpChannel.getPassword(), otpChannel.getAlias());
        } else {
            encryptor = EncryptorImpl.getInstance(otpChannel.getJcaAlgorithm(), otpChannel.getProvider(), otpChannel.getKeyBytes());
        }
        return encryptor;
    }

    private String getEncryptedPartOfMessage(List<String> listMessage) {
        return arrangeMessage(listMessage.subList(1, listMessage.size()));
    }

    private String getNonEncryptedPartOfMessage(List<String> listMessage) {
        return arrangeMessage(listMessage.subList(0, 1));
    }

    abstract String getMessageType();

    abstract Message createRequestBody(OtpMessageModel request);

    abstract Message createResponseBody();
}
