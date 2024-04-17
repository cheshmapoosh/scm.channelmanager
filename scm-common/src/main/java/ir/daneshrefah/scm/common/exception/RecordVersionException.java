package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_RECORD_VERSION_EXCEPTION;

public class RecordVersionException extends BaseException implements ErrorCodeAwareException{


    private final String source;
    public RecordVersionException(String source) {
        super("record version does not match", null);
        this.source = source;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_RECORD_VERSION_EXCEPTION;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_BUSINESS;
    }

    @Override
    public String getSource() {
        return source;
    }
}
