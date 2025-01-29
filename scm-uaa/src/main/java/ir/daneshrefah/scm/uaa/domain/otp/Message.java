package ir.daneshrefah.scm.uaa.domain.otp;

import java.util.List;

public abstract class Message {

    protected boolean isSizeOfInputListEqualGreaterThanFieldNo(List<String> listMessage) {
        return listMessage != null && listMessage.size() >= getFieldsNo();
    }

    abstract int getFieldsNo();

    abstract List<String> toList();

    abstract void setFieldsFromList(List<String> listMessage);
}
