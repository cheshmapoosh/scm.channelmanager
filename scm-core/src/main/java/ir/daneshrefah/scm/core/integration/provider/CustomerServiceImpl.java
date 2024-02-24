package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MethodNotSupportDataException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.customer.Customer;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.service.ExternalServiceProvider;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProviderDataProvider;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.plugin.api.service.CustomerSynchronizationRequest;
import ir.daneshrefah.scm.plugin.api.utils.ClassLoader;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

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
            if (null == customerProvider) {
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
    public Customer findCustomerByPersonId(ExternalServiceProvider provider, PersonProfile.PersonId personId) {
        Customer customer = findCustomerByPersonId(provider, personId.id());
        if (null == customer) {
            customer = findCustomerByPersonProfileId(provider, personId.username());
        }
        return customer;
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

    @Override
    public Customer findCustomerByProviderCode(String providerCode, Long personId) {
        return findCustomerByPersonId(serviceService.findServiceProviderByCode(providerCode), personId);
    }

    @Override
    public Customer findCustomerByProviderIdAndPersonId(String providerId, Long personId) {
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            provider = serviceService.findServiceProviderByCode(providerId);
        }
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        return findCustomerByPersonId(provider, personId);
    }

    @Override
    public Customer findCustomerByPersonId(ExternalServiceProvider provider, Long personId) {
        if (null == provider) {
            throw new MissingRequiredInputException("provider");
        }
        if (null == personId) {
            throw new MissingRequiredInputException("personId");
        }
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
    public Customer synchronizeProviderCustomerInfoByPersonId(CustomerSynchronizationRequest request) {
        if (null == request.getPersonId()) {
            throw new MissingRequiredInputException("personId");
        }
        if (StringUtils.isEmpty(request.getProviderId())) {
            throw new MissingRequiredInputException("providerId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(request.getProviderId());
        if (null == provider) {
            provider = serviceService.findServiceProviderByCode(request.getProviderId());
        }
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        Optional<ServiceProviderDataProvider> dataProvider = findCustomerDataProvider(provider);
        if (dataProvider.isEmpty()) {
            throw new MethodNotSupportDataException("'customer data provider' not found for provider '" + provider.getCode() + "'.");
        }
        GeneralPerson person = personService.findPersonByPersonId(request.getPersonId().intValue());
        if (null == person) {
            throw new InvalidInputException("personId");
        }
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
