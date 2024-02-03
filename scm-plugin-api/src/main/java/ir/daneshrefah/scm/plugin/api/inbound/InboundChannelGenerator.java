package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageBuildRequest;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
public interface InboundChannelGenerator<T> {

    boolean registerEndpoints(List<TerminalServiceAccess> services);

    boolean initConfig();

    boolean registerEndpoints();

    MessageBuildRequest extractMessageBuildRequest(T input, MessageBuildRequest request, Service service);

    public T execute(T input, TerminalServiceAccess serviceAccess);

    public Message execute(MessageBuildRequest request);

}
