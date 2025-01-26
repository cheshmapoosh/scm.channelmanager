package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.constant.AccountStatus;
import ir.daneshrefah.scm.common.constant.AssetProviderCode;
import ir.daneshrefah.scm.common.constant.CustomerRelationType;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityRequest;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityResponse;
import ir.daneshrefah.scm.common.dto.membership.CustomerProviderSyncRequest;
import ir.daneshrefah.scm.common.dto.membership.MembershipFindRequest;
import ir.daneshrefah.scm.common.dto.membership.MembershipLocalFindRequest;
import ir.daneshrefah.scm.common.dto.spec.ExternalAccountResponseData;
import ir.daneshrefah.scm.common.dto.terminal.TerminalService;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.asset.*;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.service.AssetProviderService;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.core.entity.asset.*;
import ir.daneshrefah.scm.core.mapper.AssetProviderMapper;
import ir.daneshrefah.scm.core.mapper.MembershipMapper;
import ir.daneshrefah.scm.core.mapper.MembershipTerminalAccessMapper;
import ir.daneshrefah.scm.core.repository.*;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.task.service.TaskAssetService;
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
public class CustomerServiceImpl implements CustomerService, TaskAssetService {

    private static final List<AssetProvider> ASSET_PROVIDERS_CACHE = new ArrayList<>();
    private final MembershipTerminalAccessRepository membershipTerminalAccessRepository;
    private final CustomerAccountRepository customerAccountRepository;
    private final AssetProviderRepository assetProviderRepository;
    private final ServiceProducerTemplate serviceProducerTemplate;
    private final AccountTypeRepository accountTypeRepository;
    private final AssetProviderService assetProviderService;
    private final MembershipRepository membershipRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final TerminalService terminalService;
    private final ServiceService serviceService;
    private final PersonService personService;

    @Override
    public List<Membership> findLocalMembershipList(MembershipLocalFindRequest request) {
        validateAssetsFindRequest(request);
//        AssetType assetType = request.getAssetType();
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        String personUsername = person.getUsername();
        List<MembershipEntity> foundAssets;
//        if (Objects.isNull(assetType)) {
        foundAssets = membershipRepository.findAllByPersonUsername(personUsername);
//        } else {
//            foundAssets = membershipRepository.findAllByAssetTypeAndPersonUsername(assetType, personUsername);
//        }
        return MembershipMapper.INSTANCE.toModels(foundAssets);
    }

    @Override
    public Membership findLocalMembership(String membershipId) {
        ValidationUtils.checkBlankString(membershipId, () -> new InvalidInputException("membershipId"));
        ValidationUtils.checkNumericInput(membershipId, () -> new InvalidInputException("membershipId"));
        MembershipEntity found = membershipRepository.findMembershipById(Long.parseLong(membershipId)).orElseThrow(() -> new NoMatchRecordFoundException("membershipId"));
        return MembershipMapper.INSTANCE.toModel(found);
    }

    @Override
    public List<Membership> findMembershipList(MembershipFindRequest request) {
        validateAssetsFindRequest(request);
        List<Membership> responseList = new ArrayList<>();
        responseList.addAll(Objects.requireNonNull(provideAccountTypeAssetsData(request)));
        //TODO ADD CARD AND LOAN
        return responseList;
    }


    private List<Membership> provideAccountTypeAssetsData(MembershipFindRequest request) {
        //TODO IMPORTANT TODO =>> HOW FIND SERVICE FROM ASSET PROVIDER
//        String assetProviderId = request.getAssetProviderId();
//        ValidationUtils.checkNull(assetProviderId, () -> new InvalidInputException("assetProviderId"));
//        AssetProvider assetProvider = assetProviderService.findAssetProviderById(Integer.parseInt(request.getAssetProviderId())).orElseThrow(() -> new InvalidInputException("assetProviderId"));
//        if (assetProvider.getCode().equals(AssetProviderCode.NAB)) {
//            assetProviderRepository.findById(assetProvider.getId()).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId"));
//            PersonType personType = getRequestCurrentPerson().getPersonType();
//            String nationalId = request.getNationalId();
//            checkPersonAssetAccess(personType, nationalId);
//            ir.daneshrefah.scm.common.model.service.Service service =
//                    serviceService.finServiceBy(assetProvider.getId());
//            GeneralPerson person = personService.findPerson(personType, nationalId, request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
//            List<ExternalAccountResponseData> accountList = getPersonAccountList(assetProvider, request.getPersonType(), person, service.getCode(), request.getPageNo(), request.getPageSize());
//            return MapToAccountMembership(accountList, request, assetProvider);
//        }
        return Collections.emptyList();
    }

    private List<ExternalAccountResponseData> getPersonAccountList(AssetProvider assetProvider, PersonType personType, GeneralPerson person, String serviceCode, Integer pageNo, Integer pageSize) {
        if (assetProvider.getCode().equals(AssetProviderCode.NAB)) {
            String nationalId = null;
            Map<String, String> requestMap = new HashMap<>();
            if (person instanceof GeneralRealPerson realPerson) {
                nationalId = realPerson.getNationalCode();
            } else if (person instanceof GeneralLegalPerson legalPerson) {
                nationalId = legalPerson.getNationalId();
                String subOrg = legalPerson.getSubOrganizationId();
                if (Objects.nonNull(subOrg)) {
                    requestMap.put("subOrg", subOrg);
                }
            }
            requestMap.put("nationalId", nationalId);
            if (Objects.nonNull(pageNo) && Objects.nonNull(pageSize)) {
                Integer start = (pageNo - 1) * pageSize;
                int end = start + pageSize;
                requestMap.put("start", start.toString());
                requestMap.put("end", Integer.toString(end));
            }
            ExternalAccountResponseData[] nabAccountListResponseData = serviceProducerTemplate.callService(serviceCode, requestMap, ExternalAccountResponseData[].class);
            return filterExternalAccountResponseDateList(Arrays.stream(nabAccountListResponseData).toList(), personType);
        }
        throw new InvalidInputException("assetProviderId");
    }

    private List<ExternalAccountResponseData> filterExternalAccountResponseDateList(List<ExternalAccountResponseData> list, PersonType personType) {
        return list
                .stream()
                .filter(nabAccount -> PersonType.findNabDetailCode(nabAccount.getAccountOwnerCustomerTypeCode()).equals(personType))
                .toList();
    }

    private List<Membership> MapToAccountMembership(List<ExternalAccountResponseData> accountList, MembershipFindRequest request, AssetProvider assetProvider) {
        GeneralPerson person = personService.findPerson(getRequestCurrentPerson().getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        return accountList
                .stream()
                .map(nabAccount -> {
                    //create account
                    AccountEntity accountEntity = mapToAccount(nabAccount, assetProvider);
                    Account account = new Account();
                    account.setAccountNo(accountEntity.getAccountNo());
                    AccountType accountType = new AccountType();
                    AccountTypeEntity accountTypeEntity = accountEntity.getAccountType();
                    accountType.setName(accountTypeEntity.getName());
                    accountType.setId(accountTypeEntity.getId());
                    account.setAccountType(accountType);
                    account.setClose(accountEntity.getClose());
                    account.setAssetProvider(AssetProviderMapper.INSTANCE.toModel(accountEntity.getAssetProvider()));
                    account.setCloseDate(accountEntity.getCloseDate());
                    //create customer
                    Customer customer = new Customer();
                    customer.setCustomerNo(String.valueOf(nabAccount.getAccountNumber()));
                    //create customer account
                    CustomerAccount customerAccount = new CustomerAccount();
                    customerAccount.setCustomer(customer);
                    customerAccount.setAccount(account);
                    //create membership
                    Membership membership = new Membership();
                    membership.setCustomerAccount(customerAccount);
//                    membership.setAssetType(AssetType.ACCOUNT);
                    membership.setPerson(person);
                    return membership;
                }).toList();

    }

    @Override
    public AccountFavoriteActivityResponse accountFavoriteActivity(AccountFavoriteActivityRequest request) {
        ValidationUtils.checkNullOrEmptyList(request.getAccountNoList(), () -> new InvalidInputException("accountNoList"));
        ValidationUtils.checkNull(request.getIsFavorite(), () -> new InvalidInputException("isFavorite"));
        GeneralPerson currentPerson = getRequestCurrentPerson();
        Authentication scmAuthentication = AuthenticationUtils.getScmAuthentication();
        ValidationUtils.checkNull(scmAuthentication, AuthenticationRequiredException::new);
        assert scmAuthentication != null;
        String terminalCode = scmAuthentication.getTerminalCode();
        ValidationUtils.checkNull(terminalCode, () -> new MissingRequiredInputException("terminal"));
        Terminal terminal = terminalService.findTerminalByCode(terminalCode).orElseThrow(() -> new InvalidInputException("terminal"));
        Iterable<MembershipTerminalAccessEntity> membershipTerminalAccessList = membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByPersonId(Long.valueOf(currentPerson.getId()), terminal.getId());
        Iterator<MembershipTerminalAccessEntity> iterator = membershipTerminalAccessList.iterator();
        final AccountFavoriteActivityResponse response = new AccountFavoriteActivityResponse();
        response.setIsFavorite(request.getIsFavorite());
        applyAccountsFavouriteStatus(scmAuthentication, iterator, request, response);
        if (Objects.nonNull(request.getAccountNoList())) {
            return response;
        }
        throw new InvalidInputException("accountNoList");
    }

    private void applyAccountsFavouriteStatus(Authentication authentication, Iterator<MembershipTerminalAccessEntity> iterator, AccountFavoriteActivityRequest request, AccountFavoriteActivityResponse response) {
        while (iterator.hasNext()) {
            MembershipTerminalAccessEntity entity = iterator.next();
            request.getAccountNoList()
                    .stream()
                    .filter(entity.getMembership().getCustomerAccount().getAccount().getAccountNo()::equals)
                    .findFirst()
                    .ifPresent(accountId -> {
                        entity.setFavorite(request.getIsFavorite());
                        membershipTerminalAccessRepository.save(entity);
                        List<String> accountNoList = response.getAccountNoList();
                        if (Objects.isNull(accountNoList)) {
                            accountNoList = new ArrayList<>();
                            response.setAccountNoList(accountNoList);
                        }
                        accountNoList.add(accountId);
                        updateScmProfile(authentication, entity);
                    });
        }
    }

    private void updateScmProfile(Authentication scmAuthentication, MembershipTerminalAccessEntity entity) {
        List<MembershipTerminalAccess> memberships = scmAuthentication.getProfile().getMemberships();
        if (Objects.nonNull(memberships)) {
            memberships
                    .stream()
                    .filter(membership -> membership.getId().equals(entity.getId()))
                    .findFirst()
                    .ifPresent(membership -> {
                        membership.setFavorite(entity.getFavorite());
                    });
        }
    }

    private List<Membership> syncAllMembership(List<Membership> memberships, PersonType personType, String nationalId, String subOrg, Integer assetProviderId) {
        checkPersonAssetAccess(personType, nationalId);
        ir.daneshrefah.scm.common.model.service.Service service = null; // TODO ****
//                serviceService.findAssetProviderProviderServiceByAssetProviderId(assetProviderId);
        GeneralPerson person = personService.findPerson(personType, nationalId, subOrg).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        AssetProviderEntity assetProviderEntity = assetProviderRepository.findById(assetProviderId).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId"));
        AssetProvider assetProvider = AssetProviderMapper.INSTANCE.toModel(assetProviderEntity);
        List<ExternalAccountResponseData> accountList = getPersonAccountList(assetProvider, personType, person, service.getCode());
        List<Membership> accountMembership = syncAccountMembership(accountList, filterAccountMembership(memberships), person, assetProviderId);
        List<Membership> cardMembership = new ArrayList<>(); //TODO
        List<Membership> loanMembership = new ArrayList<>(); //TODO
        return createMergedAssetsList(accountMembership, cardMembership, loanMembership);
    }

    private List<Membership> filterAccountMembership(List<Membership> memberships) {
        if (Objects.isNull(memberships)) {
            return null;
        }
        return memberships
                .stream()
//                .filter(membership -> membership.getAssetType().equals(AssetType.ACCOUNT))
                .map(membership -> MembershipMapper.INSTANCE.toModel(membershipRepository.findMembershipById(membership.getId()).orElseThrow(() -> new NoMatchRecordFoundException("membershipId"))))
                .toList();
    }

    @Override
    public List<Membership> syncMembershipList(CustomerProviderSyncRequest request) {
        validateCustomerProviderFindRequest(request);
        List<Membership> memberships = null;
        if (Objects.isNull(request.getSyncAll()) || !request.getSyncAll()) {
            if (Objects.nonNull(request.getMembershipSyncIdList()) && !request.getMembershipSyncIdList().isEmpty()) {
                memberships = request.getMembershipSyncIdList()
                        .stream()
                        .map(id -> membershipRepository.findMembershipById(Long.parseLong(id)).orElseThrow(() -> new NoMatchRecordFoundException("membershipSyncIdList")))
                        .map(MembershipMapper.INSTANCE::toModel)
                        .toList();
            } else if (Objects.nonNull(request.getAccountSyncIdList()) && !request.getAccountSyncIdList().isEmpty()) {
                memberships = new ArrayList<>();
                request.getAccountSyncIdList()
                        .stream()
                        .map(id -> membershipRepository.findMembershipByAccountNoAndUsername(id, getRequestCurrentPerson().getUsername()).orElseThrow(() -> new NoMatchRecordFoundException("membershipSyncIdList")))
                        .map(MembershipMapper.INSTANCE::toModel)
                        .forEach(memberships::add);
            }
        }
        return syncAllMembership(memberships, request.getPersonType(), request.getNationalId(), request.getSubOrganizationId(), Integer.valueOf(request.getAssetProviderId()));
    }

    private List<Membership> createMergedAssetsList(List<Membership> accountMembership, List<Membership> cardMembership, List<Membership> loanMembership) {
        List<Membership> allAssets = new ArrayList<>();
        allAssets.addAll(accountMembership);
        allAssets.addAll(cardMembership);
        allAssets.addAll(loanMembership);
        return allAssets;
    }

    private List<Membership> syncAccountMembership(
            List<ExternalAccountResponseData> accountList,
            List<Membership> memberships,
            GeneralPerson person,
            Integer assetProviderId) {
        List<ExternalAccountResponseData> accountListToSync = new ArrayList<>(accountList);
        if (Objects.nonNull(memberships) && !memberships.isEmpty()) {
            accountListToSync.clear();
            memberships
                    .forEach(membership -> accountList
                            .stream()
                            .filter(nabAccountResponseData -> membership.getCustomerAccount().getAccount().getAccountNo().equals(nabAccountResponseData.getAccountNumber().toString()))
                            .findFirst()
                            .map(accountListToSync::add)
                    );

        }
        return accountListToSync
                .stream()
                .map(nabAccount -> syncAccountMembership(person, nabAccount, assetProviderId))
                .collect(Collectors.toList());
    }

    private Membership syncAccountMembership(GeneralPerson person, ExternalAccountResponseData nabAccount, Integer assetProviderId) {
        AccountEntity accountEntity = syncAccount(nabAccount, assetProviderId);
        CustomerEntity customerEntity = syncCustomer(nabAccount);
        CustomerAccountEntity customerAccountEntity = syncCustomerAccount(customerEntity, accountEntity, nabAccount);
        MembershipEntity accountMembershipEntity = syncMembership(person, customerAccountEntity);
        return MembershipMapper.INSTANCE.toModel(accountMembershipEntity);
    }

    private MembershipEntity syncMembership(GeneralPerson person, CustomerAccountEntity customerAccountEntity) {
        Optional<MembershipEntity> membershipOptional = membershipRepository.findMembershipByAccountNoAndUsername(customerAccountEntity.getAccount().getAccountNo(), person.getUsername());
        MembershipEntity accountMembership;
        if (membershipOptional.isEmpty()) {
            accountMembership = new MembershipEntity();
            accountMembership.setCustomerAccount(customerAccountEntity);
            accountMembership.setPerson(personRepository.findById(person.getId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId")));
//            accountMembership.setAssetType(AssetType.ACCOUNT);
            accountMembership.setArchiveNumber(ArchiveUtils.calculateOneMonthArchiveNo().intValue()); //TODO
            membershipRepository.save(accountMembership);
        } else {
            accountMembership = membershipOptional.get();
        }
        return accountMembership;

    }

    private CustomerAccountEntity syncCustomerAccount(CustomerEntity customerEntity, AccountEntity accountEntity, ExternalAccountResponseData nabAccount) {
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

    private CustomerEntity syncCustomer(ExternalAccountResponseData nabAccount) {
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

    private AccountEntity syncAccount(ExternalAccountResponseData nabAccount, Integer assetProviderId) {
        Optional<AccountEntity> accountOptional = accountRepository.findByAccountNo(nabAccount.getAccountNumber().toString());
        AccountEntity accountEntity;
        if (accountOptional.isPresent()) {
            //sync
            accountEntity = accountOptional.get();
            accountEntity.setClose(AccountStatus.CLOSED.getCode() == nabAccount.getAccountStatusCode() ? 1 : 0);
            accountEntity.setCloseDate((AccountStatus.CLOSED.getCode() == nabAccount.getAccountStatusCode()) ? LocalDateTime.now() : null);
            accountEntity.setAssetProvider(assetProviderRepository.findById(assetProviderId).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId")));
            accountEntity.setAccountType(accountTypeRepository.findById(Long.parseLong(nabAccount.getAccountTypeCode().toString())).orElseThrow(() -> new InvalidInputException("accountTypeCode")));
            accountEntity = accountRepository.save(accountEntity);
        } else {
            //create
            AssetProvider assetProvider = assetProviderService.findAssetProviderById(assetProviderId).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId"));
            accountEntity = mapToAccount(nabAccount, assetProvider);
            accountRepository.saveAndFlush(accountEntity);
        }
        return accountEntity;
    }

    private AccountEntity mapToAccount(ExternalAccountResponseData nabAccount, AssetProvider assetProvider) {
        AccountEntity account = new AccountEntity();
        account.setAccountNo(nabAccount.getAccountNumber().toString());
        account.setAccountType(accountTypeRepository.findById(Long.parseLong(nabAccount.getAccountTypeCode().toString())).orElseThrow(() -> new InvalidInputException("accountTypeCode")));
        account.setClose(AccountStatus.CLOSED.getCode() == nabAccount.getAccountStatusCode() ? 1 : 0);
        account.setAssetProvider(assetProviderRepository.findById(assetProvider.getId()).orElseThrow(() -> new NoMatchRecordFoundException("assetProviderId")));
        account.setCloseDate((nabAccount.getAccountStatusCode().equals(1)) ? LocalDateTime.now() : null);
        return account;
    }

    private List<ExternalAccountResponseData> getPersonAccountList(AssetProvider assetProvider, PersonType personType, GeneralPerson person, String serviceCode) {
        return getPersonAccountList(assetProvider, personType, person, serviceCode, null, null);
    }

    private void validateCustomerProviderFindRequest(CustomerProviderSyncRequest request) {
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


    private void validateAssetsFindRequest(MembershipLocalFindRequest findRequest) {
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
    public List<MembershipTerminalAccess> findLocalMembershipTerminalAccesses(MembershipLocalFindRequest request) {
        if (null == request) {
            request = new MembershipLocalFindRequest();
        }
//        Pageable pageable = PageRequest.of(Math.max(request.getPageNo() - 1, 0), request.getPageSize());
        List<MembershipTerminalAccessEntity> entities = membershipTerminalAccessRepository.findAll(
                MembershipTerminalAccessSpecs.toSpecification(request)/*, pageable*/);
        return MembershipTerminalAccessMapper.INSTANCE.toMembershipTerminalAccessList(entities);
    }

    @Override
    public Optional<String> findCustomerNo(Integer userId) {
        return membershipRepository.findMembershipListByUserId(userId)
                .stream().map(MembershipEntity::getCustomerAccount)
                .filter(Objects::nonNull)
                .map(CustomerAccountEntity::getCustomer)
                .filter(Objects::nonNull)
                .map(CustomerEntity::getCustomerNo)
                .findFirst();
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
