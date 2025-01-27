package ir.daneshrefah.scm.uaa.domain.otp;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class RegisterNewUserResponseBody extends Message {

    @Setter
    @Getter
    private String serialNo;
    @Setter
    @Getter
    private String activationCode;
    @Setter
    @Getter
    private String username;
    private final int USERNAME_INDEX = 0;
    private final int SERIAL_NO_INDEX = 1;
    private final int ACTIVATION_CODE_INDEX = 2;
    private final int FIELDS_NO = 3;

    public RegisterNewUserResponseBody() {
    }

    @Override
    int getFieldsNo() {
        return FIELDS_NO;
    }

    public List<String> toList() {
        List<String> registerNewUserResponseBodyAsList = new ArrayList<>();
        registerNewUserResponseBodyAsList.add(USERNAME_INDEX, username);
        registerNewUserResponseBodyAsList.add(SERIAL_NO_INDEX, serialNo);
        registerNewUserResponseBodyAsList.add(ACTIVATION_CODE_INDEX, activationCode);
        return registerNewUserResponseBodyAsList;
    }

    public void setFieldsFromList(List<String> listMessage) {
        username = listMessage.get(USERNAME_INDEX);
        serialNo = listMessage.get(SERIAL_NO_INDEX);
        activationCode = listMessage.size() >= FIELDS_NO ? listMessage.get(ACTIVATION_CODE_INDEX) : null;
    }
}
