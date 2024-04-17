package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_TERMINAL_NOT_FOUND_EXCEPTION;

public class TerminalDoesNotExistException extends BaseException implements ErrorCodeAwareException{

    private final String source;
    public TerminalDoesNotExistException(String source) {
        super("terminal does not exist", null);
        this.source = source;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_TERMINAL_NOT_FOUND_EXCEPTION;
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
