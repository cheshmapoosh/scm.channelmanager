package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.constant.AccountStatus;
import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.CustomerProviderFindRequest;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.NabAccountResponseData;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.asset.AccountMembership;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.model.asset.MembershipTerminalAccess;
import ir.daneshrefah.scm.common.model.customer.AssetType;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.common.service.MembershipFindRequest;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.core.entity.asset.*;
import ir.daneshrefah.scm.core.mapper.MembershipMapper;
import ir.daneshrefah.scm.core.mapper.MembershipTerminalAccessMapper;
import ir.daneshrefah.scm.core.repository.*;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_ADMIN_CUSTOMER;

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
    private final CustomerAccountRepository customerAccountRepository;
    private final AssetProviderRepository assetProviderRepository;
    private final ServiceProducerTemplate serviceProducerTemplate;
    private final AccountTypeRepository accountTypeRepository;
    private final MembershipRepository membershipRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final TerminalService terminalService;
    private final ServiceService serviceService;
    private final PersonService personService;


    @Override
    public List<Membership> findLocalMembershipList(MembershipFindRequest request) {
        validateAssetsFindRequest(request);
        AssetType assetType = request.getAssetType();
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        String personUsername = person.getUsername();
        List<MembershipEntity> foundAssets;
        if (Objects.isNull(assetType)) {
            foundAssets = membershipRepository.findAllByPersonUsername(personUsername);
        } else {
            foundAssets = membershipRepository.findAllByAssetTypeAndPersonUsername(assetType, personUsername);
        }
        return MembershipMapper.INSTANCE.toModels(foundAssets);
    }

    @Override
    public AccountMembership findLocalAccountMembership(String membershipId) {
        ValidationUtils.checkBlankString(membershipId, () -> new InvalidInputException("membershipId"));
        ValidationUtils.checkNumericInput(membershipId, () -> new InvalidInputException("membershipId"));
        AccountMembershipEntity found = membershipRepository.findAccountMembershipById(Long.parseLong(membershipId)).orElseThrow(() -> new NoMatchRecordFoundException("membershipId"));
        return MembershipMapper.INSTANCE.toModel(found);
    }

    @Override
    public List<Membership> findProviderMembershipList(CustomerProviderFindRequest request) {
        validateCustomerProviderFindRequest(request);
        checkPersonAssetAccess(request.getPersonType(), request.getNationalId());
        ir.daneshrefah.scm.common.model.service.Service service =
                serviceService.findAssetProviderProviderServiceByAssetProviderId(Integer.parseInt(request.getAssetProviderId()));
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        //TODO : WAITING FOR NAB ACCOUNT LIST CHANGES (GET ACCOUNT LIST WITH NATIONAL ID INSTEAD OF CUSTOMER NO)
        List<NabAccountResponseData> accountList = getPersonAccountList(person, service.getCode());
        List<Membership> accountMembership = syncAccountMembership(accountList, person, request.getAssetProviderId());
        List<Membership> cardMembership = new ArrayList<>(); //TODO
        List<Membership> loanMembership = new ArrayList<>(); //TODO
        return createMergedAssetsList(accountMembership, cardMembership, loanMembership);
    }

    private List<Membership> createMergedAssetsList(List<Membership> accountMembership, List<Membership> cardMembership, List<Membership> loanMembership) {
        List<Membership> allAssets = new ArrayList<>();
        allAssets.addAll(accountMembership);
        allAssets.addAll(cardMembership);
        allAssets.addAll(loanMembership);
        return allAssets;
    }

    private List<Membership> syncAccountMembership(List<NabAccountResponseData> accountList, GeneralPerson person, String assetProviderId) {
        return accountList
                .stream()
                .map(nabAccount -> syncAccountMembership(person, nabAccount, assetProviderId))
                .collect(Collectors.toList());
    }

    private AccountMembership syncAccountMembership(GeneralPerson person, NabAccountResponseData nabAccount, String assetProviderId) {
        AccountEntity accountEntity = syncAccount(nabAccount, assetProviderId);
        CustomerEntity customerEntity = syncCustomer(nabAccount);
        CustomerAccountEntity customerAccountEntity = syncCustomerAccount(customerEntity, accountEntity,nabAccount);
        AccountMembershipEntity accountMembershipEntity = syncMembership(person, customerAccountEntity);
        return MembershipMapper.INSTANCE.toModel(accountMembershipEntity);
    }

    private AccountMembershipEntity syncMembership(GeneralPerson person, CustomerAccountEntity customerAccountEntity) {
        Optional<AccountMembershipEntity> membershipOptional = membershipRepository.findAccountMembershipByAccountNoAndUsername(customerAccountEntity.getAccount().getAccountNo(), person.getUsername());
        AccountMembershipEntity accountMembership;
        if (membershipOptional.isEmpty()) {
            accountMembership = new AccountMembershipEntity();
            accountMembership.setCustomerAccount(customerAccountEntity);
            accountMembership.setPerson(personRepository.findById(person.getId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId")));
            accountMembership.setAssetType(AssetType.ACCOUNT);
            accountMembership.setArchiveNumber(ArchiveUtils.calculateOneMonthArchiveNo().intValue()); //TODO
            membershipRepository.save(accountMembership);
        } else {
            accountMembership = membershipOptional.get();
        }
        return accountMembership;

    }

    private CustomerAccountEntity syncCustomerAccount(CustomerEntity customerEntity, AccountEntity accountEntity,NabAccountResponseData nabAccount) {
        Optional<CustomerAccountEntity> customerAccountOptional = customerAccountRepository.findByCustomerAndAccount(customerEntity, accountEntity);
        Integer customerRelationTypeCode = nabAccount.getCustomerRelationTypeCode();
        CustomerAccountEntity customerAccountEntity;
        if (customerAccountOptional.isEmpty()) {
            customerAccountEntity = new CustomerAccountEntity();
            customerAccountEntity.setCustomer(customerEntity);
            customerAccountEntity.setAccount(accountEntity);
        } else {
            customerAccountEntity = customerAccountOptional.get();
        }
        customerAccountEntity.setRelationType(CustomerRelationType.findByCode(customerRelationTypeCode));
        customerAccountRepository.save(customerAccountEntity);
        return customerAccountEntity;
    }

    private CustomerEntity syncCustomer(NabAccountResponseData nabAccount) {
        Long customerNo = nabAccount.getAccountOwnerCustomerNo();
        ValidationUtils.checkNull(customerNo, () -> new InvalidInputException("customer"));
        Optional<CustomerEntity> customerOptional = customerRepository.findByCustomerNo(String.valueOf(customerNo));
        CustomerEntity customerEntity;
        if (customerOptional.isEmpty()) {
            CustomerEntity customer = new CustomerEntity();
            customer.setCustomerNo(String.valueOf(customerNo));
            customerEntity = customerRepository.save(customer);
        } else {
            customerEntity = customerOptional.get();
        }
        return customerEntity;
    }

    private AccountEntity syncAccount(NabAccountResponseData nabAccount, String assetProviderId) {
        Optional<AccountEntity> accountOptional = accountRepository.findByAccountNo(nabAccount.getAccountNumber().toString());
        AccountEntity accountEntity;
        if (accountOptional.isPresent()) {
            //sync
            accountEntity = accountOptional.get();
            accountEntity.setClose(AccountStatus.CLOSED.getCode() == nabAccount.getAccountStatusCode() ? 1 : 0);
            accountEntity.setCloseDate((AccountStatus.CLOSED.getCode() == nabAccount.getAccountStatusCode()) ? LocalDateTime.now() : null);
            accountEntity.setAssetProvider(assetProviderRepository.findById(Integer.parseInt(assetProviderId)).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId")));
            accountEntity.setAccountType(accountTypeRepository.findById(Long.parseLong(nabAccount.getAccountTypeCode().toString())).orElseThrow(() -> new InvalidInputException("accountTypeCode")));
            accountEntity = accountRepository.save(accountEntity);
        } else {
            //create
            AssetProvider assetProvider = serviceService.findAssetProviderById(Integer.parseInt(assetProviderId));
            accountEntity = mapToAccount(nabAccount, assetProvider);
            accountRepository.saveAndFlush(accountEntity);
        }
        return accountEntity;
    }

    private AccountEntity mapToAccount(NabAccountResponseData nabAccount, AssetProvider assetProvider) {
        AccountEntity account = new AccountEntity();
        account.setAccountNo(nabAccount.getAccountNumber().toString());
        account.setAccountType(accountTypeRepository.findById(Long.parseLong(nabAccount.getAccountTypeCode().toString())).orElseThrow(() -> new InvalidInputException("accountTypeCode")));
        account.setClose(AccountStatus.CLOSED.getCode() == nabAccount.getAccountStatusCode() ? 1 : 0);
        account.setAssetProvider(assetProviderRepository.findById(assetProvider.getId()).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId")));
        account.setCloseDate((nabAccount.getAccountStatusCode().equals(1)) ? LocalDateTime.now() : null);
        return account;
    }

    private List<NabAccountResponseData> getPersonAccountList(GeneralPerson person, String serviceCode) {
        List<MembershipEntity> membershipEntityList = membershipRepository.findAllByPersonUsername(person.getUsername());
        String customerNo = null;
        if (!membershipEntityList.isEmpty()) {
            MembershipEntity membershipEntity = membershipEntityList.get(0);
            customerNo = membershipEntity.getCustomerAccount().getCustomer().getCustomerNo();
        }
        Map<String, String> requestMap = new HashMap<>();
        requestMap.put("customerNo", customerNo);
        NabAccountResponseData[] nabAccountListResponseData = serviceProducerTemplate.callService(serviceCode, requestMap, NabAccountResponseData[].class);
        return Arrays.stream(nabAccountListResponseData).toList();
    }

    private void validateCustomerProviderFindRequest(CustomerProviderFindRequest request) {
        Nationality nationality = request.getNationality();
        String nationalId = request.getNationalId();
        PersonType personType = request.getPersonType();
        String subOrganizationId = request.getSubOrganizationId();
        String assetProviderId = request.getAssetProviderId();
        ValidationUtils.checkNull(assetProviderId, () -> new InvalidInputException("assetProviderId"));
        ValidationUtils.checkNull(nationality, () -> new InvalidInputException("nationality"));
        ValidationUtils.checkBlankString(nationalId, () -> new InvalidInputException("nationalId"));
        ValidationUtils.checkNull(personType, () -> new InvalidInputException("personType"));
        ValidationUtils.checkBlankStringIfNotNull(subOrganizationId, () -> new InvalidInputException("subOrganizationId"));
        // TODO this block has been deactivated for dev environment (dev nationalCode has invalid format!)
      /*   if (personType.equals(PersonType.REAL) && !ValidationUtils.checkIsValidNationalCode(nationalId)) {
            throw new InvalidInputException("nationalId");
        }*/
    }


    private void validateAssetsFindRequest(MembershipFindRequest findRequest) {
        String nationalId = findRequest.getNationalId();
        PersonType personType = findRequest.getPersonType();
        String subOrganizationId = findRequest.getSubOrganizationId();
        ValidationUtils.checkNull(personType, () -> new InvalidInputException("personType"));
        ValidationUtils.checkBlankStringIfNotNull(subOrganizationId, () -> new InvalidInputException("subOrganizationId"));
        ValidationUtils.checkBlankString(nationalId, () -> new InvalidInputException("nationalId"));
        checkPersonAssetAccess(findRequest.getPersonType(), findRequest.getNationalId());
    }

    private void checkPersonAssetAccess(PersonType personType, String requestNationalId) {
        if (!isCustomerAdmin()) {
            GeneralPerson currentPerson = getRequestCurrentPerson();
            PersonType currentPersonType = currentPerson.getPersonType();
            if (!currentPersonType.equals(personType)) {
                throw new InvalidInputException("personType");
            }
            if (currentPerson instanceof GeneralLegalPerson legalPerson) {
                String nationalId = legalPerson.getNationalId();
                if (!requestNationalId.equals(nationalId)) {
                    throw new InvalidInputException("nationalId");
                }
            } else if (currentPerson instanceof GeneralRealPerson realPerson) {
                String nationalId = realPerson.getNationalCode();
                if (!requestNationalId.equals(nationalId)) {
                    throw new InvalidInputException("nationalId");
                }
            }
        }
    }

    private boolean isCustomerAdmin() {
        UserAuthentication loggedInUserAuthentication = AuthenticationUtils.getLoggedInUserAuthentication();
        if (Objects.nonNull(loggedInUserAuthentication)) {
            return loggedInUserAuthentication.hasAuthority(ROLE_ADMIN_CUSTOMER);
        }
        return false;
    }

    private GeneralPerson getRequestCurrentPerson() {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        ValidationUtils.checkNull(loggedInUser, AuthenticationRequiredException::new);
        GeneralPerson person = loggedInUser.getPerson();
        ValidationUtils.checkNull(person, () -> new NoMatchRecordFoundException("nationalId"));
        return person;
    }


    @Override
    public List<MembershipTerminalAccess> findMembershipTerminalAccessList(Long personId, String terminalId) {
        if (null == personId || StringUtils.isEmpty(terminalId)) {
            return null;
        }
        Iterable<MembershipTerminalAccessEntity> membershipTerminalAccessEntities =
                membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByPersonId(personId, terminalId);

        return MembershipTerminalAccessMapper.INSTANCE.toMembershipTerminalAccessList(membershipTerminalAccessEntities);
    }

    @Override
    public List<MembershipTerminalAccess> findLocalMembershipTerminalAccesses(MembershipFindRequest request) {
        if (null == request) {
            request = new MembershipFindRequest();
        }
//        Pageable pageable = PageRequest.of(Math.max(request.getPageNo() - 1, 0), request.getPageSize());
        List<MembershipTerminalAccessEntity> entities = membershipTerminalAccessRepository.findAll(
                MembershipTerminalAccessSpecs.toSpecification(request)/*, pageable*/);
        return MembershipTerminalAccessMapper.INSTANCE.toMembershipTerminalAccessList(entities);
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
