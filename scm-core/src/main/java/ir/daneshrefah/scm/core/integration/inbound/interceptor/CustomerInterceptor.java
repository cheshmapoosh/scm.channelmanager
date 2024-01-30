package ir.daneshrefah.scm.core.integration.inbound.interceptor;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.plugin.api.inbound.interceptor.MessageInterceptor;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalService;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-29
 */
@RequiredArgsConstructor
public class CustomerInterceptor extends MessageInterceptor {

    private final CustomerService customerService;

    @Override
    protected Message internalIntercept(Message message) {
        boolean isCustomerLoadedIfRequired = checkCustomerInfoIsLoaded(message);
        if (!isCustomerLoadedIfRequired) {
            message.addAccessDeniedError(null, ERROR_CODE_VALIDATION_PROVIDER_CUSTOMER_NOT_FOUND,
                    "no customer found for provider.");
            return message;
        }
        return message;
    }

    private boolean checkCustomerInfoIsLoaded(Message message) {
        Service service = message.getHeader().getServiceAccess().getService();
        PersonProfile profile = message.getHeader().getPersonProfile();
        ExternalService externalService = service instanceof ExternalService ? (ExternalService) service : null;
        if (null == externalService || !externalService.getServiceProvider().isCustomerProvided() || !externalService.isCustomerBased()) {
            return true;
        }
        if (null == profile) {
            return false;
        }
        String providerId = externalService.getServiceProvider().getId();
        if (!profile.isCustomerLoaded(providerId)) {
            customerService.fillCustomerForPersonProfile(profile, externalService.getServiceProvider());
        }

        return profile.isCustomerLoaded(providerId) && null != profile.getCustomer(providerId) &&
                StringUtils.isNotEmpty(profile.getCustomer(providerId).getCustomerNo());
    }

}
