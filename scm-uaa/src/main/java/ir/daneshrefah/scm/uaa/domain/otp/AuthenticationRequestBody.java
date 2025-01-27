package ir.daneshrefah.scm.uaa.domain.otp;

import ir.daneshrefah.scm.uaa.exception.OtpServiceException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationRequestBody extends Message {

    private String username;
    private String pin;
    @Getter
    private String password;
    private String channelId;
    private static final int USERNAME_INDEX = 3;
    private static final int PIN_INDEX = 1;
    private static final int PASSWORD_INDEX = 0;
    private static final int CHANNEL_ID_INDEX = 2;
    private static final int FIELDS_NO = 4;

    public AuthenticationRequestBody(SecondPasswordAuthenticationToken authenticationToken, String pin, String channelId) {
        this.password = authenticationToken.getCredentials().toString();
        this.username = authenticationToken.getPrincipal().toString();
        this.channelId = channelId;
        this.pin = pin;
    }

    public List<String> toList() {
        List<String> authenticationRequestBodyAsList = new ArrayList();
        authenticationRequestBodyAsList.add(password);
        authenticationRequestBodyAsList.add(pin);
        authenticationRequestBodyAsList.add(channelId);
        authenticationRequestBodyAsList.add(username);
        return authenticationRequestBodyAsList;
    }

    public void setFieldsFromList(List<String> listMessage) {
        if (!isSizeOfInputListEqualGreaterThanFieldNo(listMessage)) {
            throw new OtpServiceException();
        } else {
            this.username = listMessage.get(USERNAME_INDEX);
            this.pin = listMessage.get(PIN_INDEX);
            this.password = listMessage.get(PASSWORD_INDEX);
            this.channelId = listMessage.get(CHANNEL_ID_INDEX);
        }
    }

    @Override
    int getFieldsNo() {
        return FIELDS_NO;
    }

    public Message getMessageBody() {
        return null;
    }
}
