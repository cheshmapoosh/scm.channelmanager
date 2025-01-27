package ir.daneshrefah.scm.uaa.domain.otp;

import ir.daneshrefah.scm.uaa.exception.OtpServiceException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class OtpMessage extends Message {

    @Getter
    private String channelCode;
    @Getter
    private Message messageDetails;
    private final int CHANNEL_CODE_INDEX = 0;
    private static final int MESSAGE_BODE_FROM_INDEX = 1;
    private static final int FIELDS_NO = 1;

    public OtpMessage(String channelCode, Message messageDetails) {
        this.channelCode = channelCode;
        this.messageDetails = messageDetails;
    }

    public OtpMessage(Message messageDetails) {
        this.messageDetails = messageDetails;
    }

    public List<String> toList() {
        List<String> messageAsList = new ArrayList<>();
        messageAsList.add(CHANNEL_CODE_INDEX, channelCode);
        messageAsList.addAll(messageDetails.toList());
        return messageAsList;
    }

    public Message getMessageBody() {
        return messageDetails;
    }

    @Override
    int getFieldsNo() {
        return FIELDS_NO + messageDetails.getFieldsNo();
    }

    public void setFieldsFromList(List<String> listMessage) {
        if (!isSizeOfInputListEqualGreaterThanFieldNo(listMessage)) {
            throw new OtpServiceException();
        } else {
            this.channelCode = listMessage.get(CHANNEL_CODE_INDEX);
            messageDetails.setFieldsFromList(listMessage.subList(MESSAGE_BODE_FROM_INDEX, listMessage.size()));
        }
    }
}
