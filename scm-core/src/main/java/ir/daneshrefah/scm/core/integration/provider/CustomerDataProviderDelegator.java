package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProviderDataProvider;
import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.plugin.api.model.service.external.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.service.CustomerDataProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
@Service
@Slf4j
public class CustomerDataProviderDelegator implements CustomerDataProviderService {

    private Map<String, ServiceProviderDataProvider> providersMap;

    public CustomerDataProviderDelegator(ApplicationContext context) {
        log.info("start loading ServiceProviderDataProvider");
        providersMap = context.getBeansOfType(ServiceProviderDataProvider.class);
        log.info("end loading ServiceProviderDataProvider '{}'", providersMap.size());
    }

    @Override
    public PersonProfile fillCustomerForPersonProfile(PersonProfile profile, ExternalServiceProvider provider) {
        if (profile.isCustomerLoaded(provider.getId()))
            return profile;
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCustomerProviderClassName());
        if (null == dataProvider) {
            log.warn("no data provider found for provider '{}", provider.getCode());
            return profile;
        }
        Customer customer = dataProvider.findCustomerByPersonProfile(profile);
        profile.addCustomer(provider.getId(), customer);
        return profile;
    }

}
