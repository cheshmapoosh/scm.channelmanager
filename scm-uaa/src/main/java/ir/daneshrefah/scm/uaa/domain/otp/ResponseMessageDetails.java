package ir.daneshrefah.scm.uaa.domain.otp;

import ir.daneshrefah.scm.uaa.exception.OtpServiceException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ResponseMessageDetails extends MessageDetails {

    @Getter
    private String resultCode;
    @Getter
    private final Message additionInfo;
    private static final int FIELD_NO = 1;
    private final int RESULT_CODE_INDEX = super.getFieldsNo();
    private final int ADDITION_INFO_FROM_INDEX = RESULT_CODE_INDEX + 1;

    public ResponseMessageDetails(Message additionInfo) {
        this.additionInfo = additionInfo;
    }

    public List<String> toList() {
        List<String> listOfResponseBodyFields = new ArrayList();
        listOfResponseBodyFields.add(MESSAGE_NO_INDEX, messageNO);
        listOfResponseBodyFields.add(RESULT_CODE_INDEX, resultCode);
        listOfResponseBodyFields.addAll(additionInfo.toList());
        return listOfResponseBodyFields;
    }

    @Override
    int getFieldsNo() {
        return FIELD_NO + super.getFieldsNo();
    }

    public void setFieldsFromList(List<String> listMessage) {
        if (!isSizeOfInputListEqualGreaterThanFieldNo(listMessage)) {
            throw new OtpServiceException();
        } else {
            this.messageNO = listMessage.get(MESSAGE_NO_INDEX);
            this.resultCode = listMessage.get(RESULT_CODE_INDEX);
            if (listMessage.size() > 3) {
                this.additionInfo.setFieldsFromList(listMessage.subList(ADDITION_INFO_FROM_INDEX, listMessage.size()));
            }
        }
    }

    public Message getMessageBody() {
        return additionInfo;
    }
}