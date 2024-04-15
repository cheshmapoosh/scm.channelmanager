package ir.daneshrefah.scm.core.integration.service.interceptor;

import ir.daneshrefah.scm.common.exception.NoAssetFoundException;
import ir.daneshrefah.scm.common.exception.NoCustomerFoundException;
import ir.daneshrefah.scm.common.model.asset.Customer;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@RequiredArgsConstructor
public class CustomerEnrichInterceptor extends MessageInterceptor {

    private final CustomerService customerService;

    @Override
    protected Message internalIntercept(Message message) {
        TerminalServiceAccess serviceAccess = message.getHeader().getServiceAccess();
        ExternalService service = serviceAccess.getService() instanceof ExternalService ?
                (ExternalService) serviceAccess.getService() : null;
        if (null == service || !service.getServiceProvider().isCustomerProvided()) {
            throw new NoCustomerFoundException();
        }
        Terminal terminal = message.getHeader().getServiceAccess().getTerminal();
        PersonProfile profile = message.getHeader().getPersonProfile();
        String customerProperty = service.getCustomerProperty();

        if (!isLoadAssetRequired(serviceAccess) && isLoadCustomerRequired(serviceAccess) &&
                message.hasNonBlankProperty(customerProperty)) {
            return message;
        }

        if (!profile.isMembershipLoaded()) {
            List<MembershipTerminalAccess> memberships = customerService.findMembershipTerminalAccessList(profile.getPersonId().id(), terminal.getId());
            profile.loadMembership(memberships);
        }

        if (!profile.hasMembership(service.getServiceProvider().getId())) {
            throw new NoAssetFoundException();
        }
        if (StringUtils.isNotEmpty(customerProperty)) {
            Customer customer = profile.getCustomer(service.getServiceProvider().getId());
            message.setPayloadValue(customerProperty, customer.getCustomerNo());
        }
        return message;
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        return isLoadCustomerRequired(serviceAccess) || isLoadAssetRequired(serviceAccess);
    }

    private boolean isLoadAssetRequired(TerminalServiceAccess serviceAccess) {
        Service service = serviceAccess.getService();
        Terminal terminal = serviceAccess.getTerminal();
        return service.getCheckAccessAsset() && terminal.isSupportCheckAssetAccess();
    }

    private boolean isLoadCustomerRequired(TerminalServiceAccess serviceAccess) {
        Service service = serviceAccess.getService();
        Terminal terminal = serviceAccess.getTerminal();
        return StringUtils.isNotEmpty(service.getCustomerProperty()) && terminal.isSupportCustomerInjection();
    }

}
