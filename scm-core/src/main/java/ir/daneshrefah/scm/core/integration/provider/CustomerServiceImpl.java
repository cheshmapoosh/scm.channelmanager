package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.core.entity.asset.MembershipTerminalAccessEntity;
import ir.daneshrefah.scm.core.mapper.MembershipTerminalAccessMapper;
import ir.daneshrefah.scm.core.repository.MembershipTerminalAccessRepository;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private final MembershipTerminalAccessRepository membershipTerminalAccessRepository;
    @Override
    public List<MembershipTerminalAccess> findMembershipTerminalAccessList(Long personId, String terminalId) {
        terminalId = "a45687d9-71b7-4e7c-a97f-2e9c8a1d6efc";
        Iterable<MembershipTerminalAccessEntity> membershipTerminalAccessEntities =
                membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByPersonId(personId, terminalId);

        return MembershipTerminalAccessMapper.INSTANCE.toMembershipTerminalAccessList(membershipTerminalAccessEntities);
    }

    /*private final PersonService personService;
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
    public Customer findLocalCustomerByProviderIdAndPersonId(String providerId, PersonProfile.PersonId personId) {
        Customer customer = findLocalCustomerByProviderIdAndPersonId(providerId, personId.id());
        if (null == customer) {
            customer = findLocalCustomerByProviderIdAndPersonUsername(providerId, personId.username());
        }
        return customer;
    }

    @Override
    public <T extends Asset> List<T> findLocalCustomerAssetListByPersonId(PersonProfile.PersonId personId, Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Map.Entry<String, ServiceProviderDataProvider> entry : providersMap.entrySet()) {
            String key = entry.getKey();
            ServiceProviderDataProvider provider = entry.getValue();
            result.addAll(provider.findLocalCustomerAssetListByPersonId(personId, clazz));
        }
        return result;
    }

    @Override
    public Customer findLocalCustomerByProviderIdAndPersonId(String providerId, Long personId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new MissingRequiredInputException("providerId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            provider = serviceService.findServiceProviderByCode(providerId);
        }
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        if (!provider.isCustomerProvided() || null == personId) {
            return null;
        }
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCode());
        if (null == dataProvider) {
            log.warn("no data provider found for provider '{}", provider.getCode());
            return null;
        }
        return dataProvider.findLocalCustomerByPersonId(personId);
    }

    @Override
    public Customer findLocalCustomerByProviderIdAndPersonUsername(String providerId, String username) {
        if (StringUtils.isEmpty(providerId)) {
            throw new MissingRequiredInputException("providerId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            provider = serviceService.findServiceProviderByCode(providerId);
        }
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        if (!provider.isCustomerProvided() || StringUtils.isEmpty(username)) {
            return null;
        }
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCode());
        if (null == dataProvider) {
            log.warn("no data provider found for provider '{}", provider.getCode());
            return null;
        }
        return dataProvider.findLocalCustomerByPersonProfileId(username);
    }

    @Override
    public Customer findRemoteCustomerByProviderIdAndPersonId(String providerId, Long personId) {
        if (StringUtils.isEmpty(providerId)) {
            throw new MissingRequiredInputException("providerId");
        }
        if (null == personId) {
            throw new MissingRequiredInputException("personId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderById(providerId);
        if (null == provider) {
            provider = serviceService.findServiceProviderByCode(providerId);
        }
        if (null == provider) {
            throw new InvalidInputException("providerId");
        }
        Optional<ServiceProviderDataProvider> dataProvider = findCustomerDataProvider(provider);
        if (dataProvider.isEmpty()) {
            throw new MethodNotSupportDataException("'customer data provider' not found for provider '" + provider.getCode() + "'.");
        }
        GeneralPerson person = personService.findPersonByPersonId(personId.intValue());
        if (null == person) {
            throw new InvalidInputException("personId");
        }
        return dataProvider.get().inquireRemoteCustomerByPerson(person);
    }

    @Override
    public Customer synchronizeProviderCustomerInfoByPersonId(CustomerSynchronizationRequest request) {
        if (StringUtils.isEmpty(request.getProviderId())) {
            throw new MissingRequiredInputException("providerId");
        }
        if (null == request.getPersonId()) {
            throw new MissingRequiredInputException("personId");
        }
        ExternalServiceProvider provider = serviceService.findServiceProviderByIdOrCode(request.getProviderId());
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
        return dataProvider.get().synchronizeCustomerInfo(provider, person);
    }

    private Optional<ServiceProviderDataProvider> findCustomerDataProvider(ExternalServiceProvider provider) {
        if (null == provider || !provider.isCustomerProvided()) {
            return Optional.empty();
        }
        ServiceProviderDataProvider dataProvider = providersMap.get(provider.getCode());
        return Optional.of(dataProvider);
    }*/

}
