package ir.daneshrefah.scm.core.integration.inbound.rest.dynamicrest;

import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-03
 */
public interface RestUrlBuilder {

    public RestUrl build(TerminalServiceChannelAccess service);

}
