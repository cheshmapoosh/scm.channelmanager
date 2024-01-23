package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProviderDataProvider;
import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.utils.string.StringUtils;
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
public class CustomerServiceImpl implements CustomerService {

    private Map<String, ServiceProviderDataProvider> providersMap;

    public CustomerServiceImpl(ApplicationContext context) {
        log.info("start loading ServiceProviderDataProvider");
        providersMap = context.getBeansOfType(ServiceProviderDataProvider.class);
        log.info("end loading ServiceProviderDataProvider '{}'", providersMap.size());
    }

    @Override
    public PersonProfile fillCustomerForPersonProfile(PersonProfile profile, ExternalServiceProvider provider) {
        if (profile.isCustomerLoaded(provider.getId()))
            return profile;
        Customer customer = findCustomerByPersonId(provider, profile.getPersonId());
        if (null == customer) {
            customer = findCustomerByPersonProfileId(provider, profile.getPersonProfileId());
        }
        profile.addCustomer(provider.getId(), customer);
        return profile;
    }

    public Customer findCustomerByPersonProfileId(ExternalServiceProvider provider, String personProfileId) {
        if (!provider.isCustomerProvided() || StringUtils.isEmpty(personProfileId)) {
            return null;
        }
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCustomerProviderClassName());
        if (null == dataProvider) {
            log.warn("no data provider found for provider '{}", provider.getCode());
            return null;
        }
        return dataProvider.findCustomerByPersonProfileId(personProfileId);
    }

    public Customer findCustomerByPersonId(ExternalServiceProvider provider, Long personId) {
        if (!provider.isCustomerProvided() || null == personId) {
            return null;
        }
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCustomerProviderClassName());
        if (null == dataProvider) {
            log.warn("no data provider found for provider '{}", provider.getCode());
            return null;
        }
        return dataProvider.findCustomerByPersonId(personId);
    }

    @Override
    public Customer synchronizeProviderCustomerInfoByPersonId(ExternalServiceProvider provider, Long personId) {
        return null;
    }

}
