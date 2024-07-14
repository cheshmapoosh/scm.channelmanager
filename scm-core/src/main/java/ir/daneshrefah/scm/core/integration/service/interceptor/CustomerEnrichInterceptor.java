package ir.daneshrefah.scm.core.integration.service.interceptor;

import ir.daneshrefah.scm.common.exception.NoAssetFoundException;
import ir.daneshrefah.scm.common.exception.NoCustomerFoundException;
import ir.daneshrefah.scm.common.model.asset.Customer;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.model.service.external.AbstractExternalService;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@RequiredArgsConstructor
public class CustomerEnrichInterceptor extends MessageInterceptor {

    private final PersonProfileLoader personProfileLoader;

    @Override
    protected Message internalIntercept(Message message) {
        Service serviceAccess = message.getHeader().getService();
        AbstractExternalService service = serviceAccess instanceof AbstractExternalService ?
                (AbstractExternalService) serviceAccess : null;
        if (Objects.isNull(service) || Objects.isNull(service.getServiceProvider().getAssetProvider()) ||
                !AuthenticationUtils.isFullyAuthenticated()) {
            throw new NoCustomerFoundException();
        }
        String customerProperty = service.getCustomerProperty();

        if (!isLoadAssetRequired(serviceAccess) && isLoadCustomerRequired(serviceAccess) &&
                message.hasNonBlankProperty(customerProperty)) {
            return message;
        }

        UserProfile profile = personProfileLoader.preparePersonProfileMemberships(AuthenticationUtils.getScmAuthentication());
        if (Objects.isNull(profile) || !profile.hasMembership(service.getServiceProvider().getAssetProvider().getId())) {
            throw new NoAssetFoundException();
        }
        if (StringUtils.isNotEmpty(customerProperty)) {
            Customer customer = profile.getCustomer(service.getServiceProvider().getAssetProvider().getId());
            message.setPayloadValue(customerProperty, customer.getCustomerNo());
        }
        return message;
    }

    @Override
    protected boolean support(Service service) {
        return isLoadCustomerRequired(service) || isLoadAssetRequired(service);
    }

    private boolean isLoadAssetRequired(Service service) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        return service.getCheckAccessAsset() && terminal.isSupportCheckAssetAccess();
    }

    private boolean isLoadCustomerRequired(Service service) {
        Terminal terminal = MessageInputContext.getCurrentContext().getTerminal();
        return StringUtils.isNotEmpty(service.getCustomerProperty()) && terminal.isSupportCustomerInjection();
    }

}
