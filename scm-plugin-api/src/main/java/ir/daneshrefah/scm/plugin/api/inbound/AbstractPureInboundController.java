package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Channel;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2020-07-06
 */
public abstract class AbstractPureInboundController {

    private InboundExecutor executor;

    protected final Channel getChannel() {
        return executor.getChannel();
    }

    protected final Message executeService() {
        return executor.executeService();
    }

    public void setExecutor(InboundExecutor executor) {
        this.executor = executor;
    }

}
