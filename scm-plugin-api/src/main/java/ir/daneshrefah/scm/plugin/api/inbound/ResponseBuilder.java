package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public interface ResponseBuilder<T> {

    public T build(T input, Message message);

}
