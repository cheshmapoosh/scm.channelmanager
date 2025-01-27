package ir.daneshrefah.scm.uaa.domain.otp;

import lombok.Getter;

public abstract class MessageDetails extends Message {

    @Getter
    protected String messageNO;
    protected final int MESSAGE_NO_INDEX = 0;
    private static final int FIELDS_NO = 1;

    public MessageDetails(String messageNO) {
        this.messageNO = messageNO;
    }

    public MessageDetails() {
    }

    @Override
    int getFieldsNo() {
        return FIELDS_NO;
    }
}
