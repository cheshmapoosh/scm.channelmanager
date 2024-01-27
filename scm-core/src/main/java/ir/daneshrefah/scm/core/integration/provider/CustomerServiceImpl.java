package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.type.Nationality;
import ir.daneshrefah.scm.common.data.type.PersonType;
import ir.daneshrefah.scm.common.exception.ValidationException;
import ir.daneshrefah.scm.common.model.person.Customer;
import ir.daneshrefah.scm.common.model.person.PersonProfile;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProviderDataProvider;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.plugin.api.service.PersonService;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.*;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final PersonService personService;
    private final ServiceService serviceService;
    private Map<String, ServiceProviderDataProvider> providersMap;

    @PostConstruct
    private void initProviderMap() {
        log.info("start loading ServiceProviderDataProvider");
//        Map<String, ServiceProviderDataProvider> providersBeanMap = context.getBeansOfType(ServiceProviderDataProvider.class);
        List<ExternalServiceProvider> providers = serviceService.findServiceProviderList();
        for (Iterator<ExternalServiceProvider> iterator = providers.iterator(); iterator.hasNext(); ) {
            ExternalServiceProvider provider = iterator.next();
            if (!provider.isCustomerProvided()) {
                continue;
            }
            String className = provider.getCustomerProviderClassName();
            ServiceProviderDataProvider customerProvider = ClassLoader.findBeanOrCreateInstanceOfClass(
                    className, ServiceProviderDataProvider.class);
            if (null ==  customerProvider) {
                log.warn("error on init customer data provider for provider '" + provider.getCode() + "'");
            }
            customerProvider.init(provider);
            if (null == providersMap) {
                providersMap = new HashMap<>();
            }
            providersMap.put(provider.getCode(), customerProvider);
        }
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
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCode());
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
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCode());
        if (null == dataProvider) {
            log.warn("no data provider found for provider '{}", provider.getCode());
            return null;
        }
        return dataProvider.findCustomerByPersonId(personId);
    }

    @Override
    public Customer synchronizeProviderCustomerInfoByPersonId(ExternalServiceProvider provider, Long personId) {
        Optional<ServiceProviderDataProvider> dataProvider = findCustomerDataProvider(provider);
        if (dataProvider.isEmpty()) {
            throw new ValidationException(provider.getCode(), ERROR_CODE_VALIDATION_SERVICE_EXTERNAL_PROVIDER_NOT_SUPPORT_CUSTOMER,
                    "'customer data provider' not found for provider '" + provider.getCode() + "'.");
        }
        GeneralPerson person = personService.findPersonByPersonId(personId);
        Customer customer = dataProvider.get().inquireCustomerByPerson(person);
        return null;
    }

    private Optional<ServiceProviderDataProvider> findCustomerDataProvider(ExternalServiceProvider provider) {
        if (null == provider || !provider.isCustomerProvided()) {
            return Optional.empty();
        }
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCode());
        return Optional.of(dataProvider);
    }
}
