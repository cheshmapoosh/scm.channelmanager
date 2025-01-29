package ir.daneshrefah.scm.uaa.domain.otp;

import java.util.ArrayList;
import java.util.List;

public class AuthenticationResponseBody extends Message {

    @Override
    int getFieldsNo() {
        return 0;
    }

    public List<String> toList() {
        return new ArrayList<>();
    }

    public void setFieldsFromList(List<String> listMessage) {
    }

    public Message getMessageBody() {
        return null;
    }
}
