package ir.daneshrefah.scm.uaa.domain.otp;

import ir.daneshrefah.scm.uaa.exception.OtpServiceException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class RequestMessageDetails extends MessageDetails {

    @Getter
    private String messageType;
    @Getter
    private Message messageBody;

    private final int MESSAGE_TYPE_INDEX = super.getFieldsNo();
    private final int MESSAGE_BODY_FROM_INDEX = MESSAGE_TYPE_INDEX + 1;
    private static final int FIELDS_NO = 1;

    public RequestMessageDetails(String messageNO, String messageType, Message messageBody) {
        super(messageNO);
        this.messageType = messageType;
        this.messageBody = messageBody;
    }

    @Override
    int getFieldsNo() {
        return FIELDS_NO + super.getFieldsNo() + messageBody.getFieldsNo();  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<String> toList() {
        List<String> listRequestMessageDetails = new ArrayList<>();
        listRequestMessageDetails.add(MESSAGE_NO_INDEX, messageNO);
        listRequestMessageDetails.add(MESSAGE_TYPE_INDEX, messageType);
        listRequestMessageDetails.addAll(messageBody.toList());
        return listRequestMessageDetails;
    }

    public void setFieldsFromList(List<String> listMessage) {
        if (!isSizeOfInputListEqualGreaterThanFieldNo(listMessage)) {
            throw new OtpServiceException();
        } else {
            this.messageNO = listMessage.get(MESSAGE_NO_INDEX);
            this.messageType = listMessage.get(MESSAGE_TYPE_INDEX);
            this.messageBody.setFieldsFromList(listMessage.subList(MESSAGE_BODY_FROM_INDEX, listMessage.size()));
        }
    }
}
