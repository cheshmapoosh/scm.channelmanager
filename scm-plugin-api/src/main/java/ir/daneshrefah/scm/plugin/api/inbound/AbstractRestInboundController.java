package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageInput;
import ir.daneshrefah.scm.common.model.terminal.Channel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public abstract class AbstractRestInboundController {

    private HttpInboundExecutor executor;

    protected final Channel getChannel() {
        return executor.getChannel();
    }

    protected final Message executeService(MessageInput messageInput) {
        return executor.executeService(messageInput);
    }

    public void setExecutor(HttpInboundExecutor executor) {
        this.executor = executor;
    }

}
