package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.common.model.authority.Authority;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-07-22
 */
public abstract class AbstractInboundChannelGenerator {

    protected Channel channel;
    protected List<TerminalServiceChannelAccess> channelAccesses;
    protected ServiceProducerTemplate producerTemplate;
    private List<Authority> authorities;

    public final void initInbound(ServiceProducerTemplate producerTemplate,
                                  Channel channel, List<TerminalServiceChannelAccess> channelAccesses, List<Authority> authorities) {
        this.producerTemplate = producerTemplate;
        this.channel = channel;
        this.channelAccesses = channelAccesses;
        this.authorities = authorities;
        initConfig();
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            registerTerminalService(channelAccess);
        }
        finalizeConfig();
    }

    private boolean checkServiceCallAllowed(TerminalServiceChannelAccess service, Message message) {
        if (!isServiceAuthenticationAllowed(service, message))
            return false;
        if (!isServiceSecondAuthenticationAllowed(service, message))
            return false;
        if (!isAccountAuthorizationAllowed(service, message))
            return false;
        if (!isServiceAccessAllowed(service, message))
            return false;
        return isServiceWithdrawAllowed(service, message);
    }

    private boolean isServiceSecondAuthenticationAllowed(TerminalServiceChannelAccess service, Message message) {
        return true;
    }

    private boolean isServiceAccessAllowed(TerminalServiceChannelAccess service, Message message) {
        Terminal terminal = service.getTerminalServiceAccess().getTerminal();
//        if ()
//        TODO
//        1) check terminal support "checkServiceAccess". if not return true
//        2) check service support "checkServiceAccess". if not return true
//        3) iterate over ServiceAccessAuthority (UserServiceAccessAuthority/AccountServiceAccessAuthority) authorities of user. if any matched (with/without account) return true
        return true;
    }

    private boolean isServiceAuthenticationAllowed(TerminalServiceChannelAccess serviceAccess, Message message) {
        Terminal terminal = serviceAccess.getTerminalServiceAccess().getTerminal();
        Service service = serviceAccess.getTerminalServiceAccess().getService();
        if (!terminal.getSupportCheckAuthentication()) {
            return true;
        }
        if (!service.getCheckAccessFirstAuthentication()) {
            return true;
        }
//        TODO
//        1) check terminal support "checkAuthentication" and "checkSecondAuthentication". if not return true
//        2) check service support "checkAuthentication" and "checkSecondAuthentication". if not return true
//        3) if authentication matched return true
        return true;
    }

    private boolean isAccountAuthorizationAllowed(TerminalServiceChannelAccess service, Message message) {
//        TODO
//        1) check terminal support "checkAccountAuthorization". if not return true
//        2) check service support "checkAccountAuthorization". if not return true
//        3) iterate over UserAccountAuthority list. if any matched return true
        return true;
    }

    private boolean isServiceWithdrawAllowed(TerminalServiceChannelAccess service, Message message) {
//        for (Iterator<TerminalServiceWithdrawAuthority> iterator = serviceLimitations.iterator(); iterator.hasNext(); ) {
//            TerminalServiceWithdrawAuthority serviceLimitation = iterator.next();
//            if (!serviceLimitation.isGranted(service, message))
//                return false;
//        }
        return true;
    }

    protected abstract void finalizeConfig();

    protected abstract void initConfig();

    protected abstract void registerTerminalService(TerminalServiceChannelAccess channelAccess);

    protected final Message invokeService(TerminalServiceChannelAccess service, Message message) {
        if (!checkServiceCallAllowed(service, message)) {
            return message;
        }
        producerTemplate.callService(service.getTerminalServiceAccess().getService(), message);
        return message;
    }

}
