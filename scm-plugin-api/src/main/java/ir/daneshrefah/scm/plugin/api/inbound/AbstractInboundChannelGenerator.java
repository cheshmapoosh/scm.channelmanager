package ir.daneshrefah.scm.plugin.api.inbound;

import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.model.limitation.ServiceLimitation;
import ir.daneshrefah.scm.plugin.api.model.message.Message;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import ir.daneshrefah.scm.plugin.api.model.terminal.Terminal;
import ir.daneshrefah.scm.plugin.api.model.terminal.TerminalServiceChannelAccess;
import org.springframework.context.ApplicationContext;

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

    private List<ServiceLimitation> serviceLimitations;
    protected Channel channel;
    protected List<TerminalServiceChannelAccess> channelAccesses;
    protected ServiceProducerTemplate producerTemplate;
    protected ApplicationContext applicationContext;
    public AbstractInboundChannelGenerator(ApplicationContext applicationContext, ServiceProducerTemplate producerTemplate,
                                           Channel channel, List<ServiceLimitation> serviceLimitations) {
        this.producerTemplate = producerTemplate;
        this.channel = channel;
        this.applicationContext = applicationContext;
        this.serviceLimitations = serviceLimitations;
    }

    public void setChannelAccesses(List<TerminalServiceChannelAccess> channelAccesses) {
        this.channelAccesses = channelAccesses;
    }

    public final void initInbound() {
        initConfig();
        for (Iterator<TerminalServiceChannelAccess> iterator = channelAccesses.iterator(); iterator.hasNext(); ) {
            TerminalServiceChannelAccess channelAccess = iterator.next();
            registerTerminalService(channelAccess);
        }
        finalizeConfig();
    }

    private boolean isServiceCallAllowed(TerminalServiceChannelAccess service, Message message) {
        if (!isServiceAuthenticationAllowed(service, message))
            return false;
        if (!isAccountAuthorizationAllowed(service, message))
            return false;
        if (!isServiceAccessAllowed(service, message))
            return false;
        return isServiceWithdrawAllowed(service, message);
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

    private boolean isServiceAuthenticationAllowed(TerminalServiceChannelAccess service, Message message) {
        Terminal terminal = service.getTerminalServiceAccess().getTerminal();
        if (!terminal.getSupportCheckAuthentication() && !terminal.getSupportCheckSecondAuthentication())
            return true;
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
        for (Iterator<ServiceLimitation> iterator = serviceLimitations.iterator(); iterator.hasNext(); ) {
            ServiceLimitation serviceLimitation = iterator.next();
            if (!serviceLimitation.isAllowServiceCall(service, message))
                return false;
        }
        return true;
    }

    protected abstract void finalizeConfig();

    protected abstract void initConfig();

    protected abstract void registerTerminalService(TerminalServiceChannelAccess channelAccess);

    protected final Message invokeService(TerminalServiceChannelAccess service, Message message) {
        if (!isServiceCallAllowed(service, message)) {
            return message;
        }
        producerTemplate.callService(service.getTerminalServiceAccess().getService(), message);
        return message;
    }

    public static String getProtocolKey() {
        return null;
    }

}
