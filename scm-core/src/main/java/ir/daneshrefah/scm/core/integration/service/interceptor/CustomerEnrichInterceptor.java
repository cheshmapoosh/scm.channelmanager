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
        ExternalService service = message.getHeader().getServiceAccess().getService() instanceof ExternalService ?
                (ExternalService) message.getHeader().getServiceAccess().getService() : null;
        if (null == service || !service.getServiceProvider().isCustomerProvided()) {
            throw new NoCustomerFoundException();
        }
        Terminal terminal = message.getHeader().getServiceAccess().getTerminal();
        PersonProfile profile = message.getHeader().getPersonProfile();

        if (!profile.isMembershipLoaded()) {
            List<MembershipTerminalAccess> memberships = customerService.findMembershipTerminalAccessList(profile.getPersonId().id(), terminal.getId());
            profile.loadMembership(memberships);
        }

        if (!profile.hasMembership(service.getServiceProvider().getId())) {
            throw new NoAssetFoundException();
        }
        String customerProperty = service.getCustomerProperty();
        if (StringUtils.isNotEmpty(customerProperty)) {
            Customer customer = profile.getCustomer(service.getServiceProvider().getId());
            message.setPayloadValue(customerProperty, customer.getCustomerNo());
        }
        return message;
    }

    @Override
    protected boolean support(TerminalServiceAccess serviceAccess) {
        Service service = serviceAccess.getService();
        Terminal terminal = serviceAccess.getTerminal();
        boolean isLoadAssetRequired = service.getCheckAccessAsset() && terminal.isSupportCheckAssetAccess();
        boolean isLoadCustomerRequired = StringUtils.isNotEmpty(service.getCustomerProperty());
        return isLoadCustomerRequired || isLoadAssetRequired;
    }

}
