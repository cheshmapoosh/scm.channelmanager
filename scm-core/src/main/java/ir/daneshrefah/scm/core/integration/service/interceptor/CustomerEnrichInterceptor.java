package ir.daneshrefah.scm.core.integration.service.interceptor;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_ASSET_NOT_FOUND;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND;

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
        boolean isCustomerLoadedIfRequired = loadCustomerIfRequired(message);
        if (!isCustomerLoadedIfRequired) {
            message.addAccessDeniedError(null, ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND,
                    "no customer found for provider.");
            return message;
        }
        boolean isCustomerAppended = appendCustomerNoIfRequired(message);
        boolean isAssetLoadedIfRequired = loadAssetsIfRequired(message);
        if (!isAssetLoadedIfRequired) {
            message.addAccessDeniedError(null, ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_ASSET_NOT_FOUND,
                    "no customer asset found for provider.");
            return message;
        }
        return message;
    }

    private boolean loadAssetsIfRequired(Message message) {
        ExternalService service = message.getHeader().getServiceAccess().getService() instanceof ExternalService ?
                (ExternalService) message.getHeader().getServiceAccess().getService() : null;
        Terminal terminal = message.getHeader().getServiceAccess().getTerminal();
        if (null == service || !terminal.isSupportCheckAssetAccess() || !service.getCheckAccessAsset()) {
            return true;
        }
        PersonProfile profile = message.getHeader().getPersonProfile();
        String providerId = service.getServiceProvider().getId();
        if (null == profile || !profile.isCustomerLoaded(providerId)) {
            return false;
        }
        if (!profile.isCustomerAssetLoaded(providerId)) {
            throw new RuntimeException("this code should be implemented.");
//            Customer customer = customerService.findCustomerByPersonId(service.getServiceProvider(), profile.getPersonId());
//            profile.addCustomer(providerId, customer);
        }
        return profile.isCustomerAssetLoaded(providerId);
    }

    private boolean appendCustomerNoIfRequired(Message message) {
        ExternalService service = message.getHeader().getServiceAccess().getService() instanceof ExternalService ?
                (ExternalService) message.getHeader().getServiceAccess().getService() : null;
        String customerProperty = null != service ? service.getCustomerProperty() : null;
        if (null == service || StringUtils.isEmpty(customerProperty) ||
                !message.getHeader().getServiceAccess().getTerminal().isSupportCustomerInjection()) {
            return true;
        }
        Customer customer = message.getHeader().getPersonProfile().getCustomer(service.getServiceProvider().getId());
        message.setPayloadValue(customerProperty, customer.getCustomerNo());
        return true;
    }

    private boolean loadCustomerIfRequired(Message message) {
        ExternalService service = message.getHeader().getServiceAccess().getService() instanceof ExternalService ?
                (ExternalService) message.getHeader().getServiceAccess().getService() : null;
        Terminal terminal = message.getHeader().getServiceAccess().getTerminal();
        if (null == service || !service.getServiceProvider().isCustomerProvided()) {
            return true;
        }
        if (!terminal.isSupportCustomerInjection() && StringUtils.isEmpty(service.getCustomerProperty()) &&
                !terminal.isSupportCheckAssetAccess() && !service.getCheckAccessAsset()) {
            return true;
        }
        PersonProfile profile = message.getHeader().getPersonProfile();
        if (null == profile) {
            return false;
        }
        String providerId = service.getServiceProvider().getId();
        if (!profile.isCustomerLoaded(providerId)) {
            Customer customer = customerService.findCustomerByPersonId(service.getServiceProvider(), profile.getPersonId());
            profile.addCustomer(providerId, customer);
        }
        boolean isCustomerLoaded = profile.isCustomerLoaded(providerId) && null != profile.getCustomer(providerId) &&
                StringUtils.isNotEmpty(profile.getCustomer(providerId).getCustomerNo());
        return isCustomerLoaded;
    }

}
