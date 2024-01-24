package ir.daneshrefah.scm.plugin.api.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-01-24
 */
@Getter
public class MessageBuildException extends BaseException {

    private MessageBuildRequest request;
    private TerminalServiceAccess serviceAccess;

    public MessageBuildException(MessageBuildRequest request, TerminalServiceAccess serviceAccess, Throwable cause) {
        super(null != cause ? cause.getMessage() : "error create message for " + serviceAccess.getService().getCode(), cause);
        this.request = request;
        this.serviceAccess = serviceAccess;
    }

    @Override
    public String getSource() {
        return serviceAccess.getService().getCode();
    }
}
