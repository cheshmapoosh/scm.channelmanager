package ir.daneshrefah.scm.core.integration.provider;

import ir.daneshrefah.scm.common.annotation.LegacyChannelManger;
import ir.daneshrefah.scm.common.constant.*;
import ir.daneshrefah.scm.common.data.entity.asset.*;
import ir.daneshrefah.scm.common.data.mapper.ChannelMapper;
import ir.daneshrefah.scm.common.data.mapper.MembershipMapper;
import ir.daneshrefah.scm.common.data.mapper.MembershipTerminalAccessMapper;
import ir.daneshrefah.scm.common.data.mapper.MembershipTerminalServiceAccessMapper;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.repository.assets.*;
import ir.daneshrefah.scm.common.data.service.assets.ChannelServiceAccessService;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityRequest;
import ir.daneshrefah.scm.common.dto.AccountFavoriteActivityResponse;
import ir.daneshrefah.scm.common.dto.asset.*;
import ir.daneshrefah.scm.common.dto.membership.*;
import ir.daneshrefah.scm.common.exception.AuthenticationRequiredException;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.asset.*;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.person.GeneralLegalPerson;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.model.person.GeneralRealPerson;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.service.AssetProviderService;
import ir.daneshrefah.scm.core.mapper.AssetProviderMapper;
import ir.daneshrefah.scm.core.services.gateway.ChannelService;
import ir.daneshrefah.scm.plugin.api.config.MembershipConfigProperty;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.CustomerService;
import ir.daneshrefah.scm.task.service.TaskAssetService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.ArchiveUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_ADMIN_CUSTOMER;
import static ir.daneshrefah.scm.common.dto.asset.MembershipSync.MembershipSyncStatus.DELETED;

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
    private static final List<Integer> DEFAULT_EB_SERVICES_ID_LIST_CACHE = new ArrayList<>();
    private static final Map<Integer, ServiceCategory> DEFAULT_SERVICE_CATEGORY_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, List<LegacyChannelServiceAccess>> TERMINAL_CODE_CSA_CACHE = new ConcurrentHashMap<>();
    private final MembershipTerminalServiceAccessRepository membershipTerminalServiceAccessRepository;
    private final MembershipTerminalServiceAccessMapper membershipTerminalServiceAccessMapper;
    private final MembershipTerminalAccessRepository membershipTerminalAccessRepository;
    private final ChannelServiceAccessRepository channelServiceAccessRepository;
    private final MembershipTerminalAccessMapper membershipTerminalAccessMapper;
    private final ChannelServiceAccessService channelServiceAccessService;
    private final CustomerAccountRepository customerAccountRepository;
    private final MembershipConfigProperty membershipConfigProperty;
    private final AssetProviderRepository assetProviderRepository;
    private final ServiceProducerTemplate serviceProducerTemplate;
    private final AccountTypeRepository accountTypeRepository;
    private final AssetProviderService assetProviderService;
    private final MembershipRepository membershipRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PersonRepository personRepository;
    private final MembershipMapper membershipMapper;
    private final ChannelService channelService;
    private final PersonService personService;
    private final ChannelMapper channelMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Membership> findLocalMembershipList(MembershipLocalFindRequest request) {
        //TODO ADD ROLE CHECKING
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        String personUsername = person.getUsername();
        if (Objects.nonNull(request.getTerminal())) {
            TerminalType terminalCode = TerminalType.findByTerminalCode(request.getTerminal());
            return membershipTerminalAccessRepository
                    .findMembershipTerminalAccessEntitiesByPersonId(person.getId(), terminalCode.getLegacyTerminalId())
                    .stream()
                    .map(MembershipTerminalAccessEntity::getMembership)
                    .map(membershipMapper::toModel)
                    .toList();

        } else {
            return membershipRepository
                    .findAllByPersonUsername(personUsername)
                    .stream()
                    .map(membershipMapper::toModel)
                    .toList();
        }
    }

    @Override
    public Membership findLocalAccountMembership(String membershipId) {
        ValidationUtils.checkBlankString(membershipId, () -> new InvalidInputException("membershipId"));
        ValidationUtils.checkNumericInput(membershipId, () -> new InvalidInputException("membershipId"));
        MembershipEntity found = membershipRepository.findAccountMembershipById(Long.parseLong(membershipId)).orElseThrow(() -> new NoMatchRecordFoundException("membershipId"));
        return membershipMapper.toModel(found);
    }

    @Override
    public List<Membership> findMembershipList(MembershipFindRequest request) {
        validateAssetsFindRequest(request);
        return new ArrayList<>(Objects.requireNonNull(provideAccountTypeAssetsData(request)));
    }

    @LegacyChannelManger
    private List<Integer> getDefaultMembershipEbServicesId() {
        if (DEFAULT_EB_SERVICES_ID_LIST_CACHE.isEmpty()) {
            synchronized (DEFAULT_EB_SERVICES_ID_LIST_CACHE) {
                if (DEFAULT_EB_SERVICES_ID_LIST_CACHE.isEmpty()) {
                    membershipConfigProperty.getDefaultServices()
                            .stream()
                            .map(serviceCode -> jdbcTemplate
                                    .query("select * from REF.EB_SERVICE where code = ?",
                                            (rs, rowNum) -> rs.getInt("EB_SERVICE_ID"), serviceCode)
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() -> new NoMatchRecordFoundException(serviceCode)))
                            .forEach(DEFAULT_EB_SERVICES_ID_LIST_CACHE::add);
                }
            }
        }
        return DEFAULT_EB_SERVICES_ID_LIST_CACHE;
    }

    @LegacyChannelManger
    private List<LegacyChannelServiceAccess> getDefaultLegacyChannelServiceAccess(String terminalCode) {
        Channel channel = channelService.findChannelByCode(terminalCode).orElseThrow(() -> new NoMatchRecordFoundException("terminalCode"));
        return TERMINAL_CODE_CSA_CACHE.computeIfAbsent(terminalCode, key -> {
            List<LegacyChannelServiceAccess> legacyChannelServiceAccessList = new ArrayList<>();
            getDefaultMembershipEbServicesId()
                    .stream()
                    .flatMap(ebServiceId -> jdbcTemplate
                            .query("select * from REF.CHANNEL_SERVICE_ACCESS where ACTIVE = '1' and EB_SERVICE_ID = ? and CHANNEL_ID = ?",
                                    (rs, rowNum) -> new LegacyChannelServiceAccess()
                                            .setChannelServiceAccessId(rs.getLong("CHANNEL_SERVICE_ACCESS_ID"))
                                            .setWithdrawalAmount(rs.getBigDecimal("WITHDRAWAL_AMOUNT")), ebServiceId, channel.getId())
                            .stream())
                    .forEach(legacyChannelServiceAccessList::add);
            return legacyChannelServiceAccessList;
        });
    }

    @LegacyChannelManger
    public void createLegacyMembershipChannelAccess(Long membershipChannelAccessId, LegacyChannelServiceAccess legacyChannelServiceAccess) {
        Long id = generateSequenceId();
        int archiveNo = ArchiveUtils.calculateOneMonthArchiveNo().intValue();
        BigDecimal maxWithdrawalPerTx = legacyChannelServiceAccess.getWithdrawalAmount();
        Long channelEbAccessId = legacyChannelServiceAccess.getChannelServiceAccessId();
        String query = "INSERT INTO REF.MEMBERSHIP_CHANNEL_SERVICE_ACCESS (ARCHIVE_NO, MCSAS_ID, MCS_ID, MAX_WITHDRAWAL_PER_TRANSACTION, CHANNEL_EB_ACCESS_ID) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.execute(query, (PreparedStatementCallback<Object>) ps -> {
            ps.setInt(1, archiveNo);
            ps.setLong(2, id);
            ps.setLong(3, membershipChannelAccessId);
            ps.setBigDecimal(4, maxWithdrawalPerTx);
            ps.setLong(5, channelEbAccessId);
            return ps.executeUpdate();
        });
    }

    @LegacyChannelManger
    private Long generateSequenceId() {
        return jdbcTemplate.query("SELECT NEXTVAL FOR REF.SQMCSAS AS ID FROM SYSIBM.SYSDUMMY1",
                (rs, rowNum) -> rs.getLong("ID")).stream().findFirst().orElseThrow(() -> new NoMatchRecordFoundException("id"));
    }

    @Override
    public List<ChannelServiceAccess> findAllChannelServiceAccessList(ChannelServiceAccessFindRequest request) {
        TerminalType terminalCode = TerminalType.findByTerminalCode(request.getTerminalCode());
        return channelServiceAccessService.findAllByTerminalType(terminalCode);
    }

    private List<Membership> provideAccountTypeAssetsData(MembershipFindRequest request) {
        String assetProviderId = request.getAssetProviderId();
        ValidationUtils.checkNull(assetProviderId, () -> new InvalidInputException("assetProviderId"));
        AssetProvider assetProvider = assetProviderService.findAssetProviderById(Integer.parseInt(request.getAssetProviderId())).orElseThrow(() -> new InvalidInputException("assetProviderId"));
        if (assetProvider.getCode().equals(AssetProviderCode.NAB)) {
            PersonType personType = getRequestCurrentPerson().getPersonType();
            String nationalId = request.getNationalId();
            checkPersonAssetAccess(nationalId);
            ir.daneshrefah.scm.common.model.service.Service service = assetProvider.getService();
            GeneralPerson person = personService.findPerson(personType, nationalId, request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
            List<ExternalAccountResponseData> accountList = findRemoteMemberships(assetProvider, person, service.getCode(), request.getPageNo(), request.getPageSize());
            return MapToAccountMembership(accountList, request, assetProvider);
        }
        return Collections.emptyList();
    }

    private List<ExternalAccountResponseData> findRemoteMemberships(AssetProvider assetProvider, GeneralPerson person, String serviceCode, Integer pageNo, Integer pageSize) {
        if (assetProvider.getCode().equals(AssetProviderCode.NAB)) { // TODO IMPL ANOTHER ASSET PROVIDER
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
            ExternalAccountResponseData[] nabAccountListResponseData = serviceProducerTemplate.callServiceWithException(serviceCode, requestMap, ExternalAccountResponseData[].class);
            return filterActiveAccountResponseDateList(Arrays.stream(nabAccountListResponseData).toList());
        }
        throw new InvalidInputException("assetProviderId");
    }

    private List<ExternalAccountResponseData> filterActiveAccountResponseDateList(List<ExternalAccountResponseData> list) {
        return list
                .stream()
                .filter(remoteAccount -> AccountStatus.ACTIVE.getCode() == remoteAccount.getAccountStatusCode())
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
        Channel channel = channelService.findChannelByCode(terminalCode).orElseThrow(() -> new InvalidInputException("terminal"));
        Iterable<MembershipTerminalAccessEntity> membershipTerminalAccessList = membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByPersonId(currentPerson.getId(), channel.getId());
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

    @Override
    @Transactional
    public List<String> assignMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request) {
        Channel channel = channelService.findChannelByCode(request.getTerminalCode()).orElseThrow(() -> new NoMatchRecordFoundException("terminalCode"));
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        LegacyTerminalDetail legacyTerminalDetail = findLegacyTerminalDetail(channel.getId());
        return findLocalMemberships(person, request.getAccountNumbers())
                .stream()
                .map(membership -> {
                    membershipTerminalAccessRepository
                            .findMembershipTerminalAccessEntitiesByMembership_IdAndChannelCode(membership.getId(), channel.getCode())
                            .ifPresentOrElse(mtca -> {
                                mtca.setActive(true);
                                membershipTerminalAccessRepository.save(mtca);
                            }, () -> {
                                MembershipTerminalAccessEntity entity = new MembershipTerminalAccessEntity();
                                entity.setActive(true);
                                entity.setChannel(channelMapper.toEntity(channel));
                                entity.setMembership(membershipMapper.toEntity(membership));
                                entity.setFavorite(false);
                                entity.setMaxWithdrawalPerDay(legacyTerminalDetail.getMaxWithdrawalPerDay());
                                entity.setFromDate(LocalDate.now());
                                entity.setToDate(LocalDate.now().plusYears(10));
                                MembershipTerminalAccessEntity saved = membershipTerminalAccessRepository.saveAndFlush(entity);
                                getDefaultLegacyChannelServiceAccess(channel.getCode())
                                        .forEach(csa -> createLegacyMembershipChannelAccess(saved.getId(), csa));
                            });
                    return membership.getCustomerAccount().getAccount().getAccountNo();
                })
                .toList();
    }

    @Override
    @Transactional
    public List<String> revokeMembershipTerminalAccess(MembershipChannelAccessAssignmentRequest request) {
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        Channel channel = channelService.findChannelByCode(request.getTerminalCode()).orElseThrow(() -> new NoMatchRecordFoundException("terminalCode"));
        List<MembershipTerminalAccessEntity> result = membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByPersonId(person.getId(), channel.getId());
        return result
                .stream()
                .filter(mca -> Objects.isNull(request.getAccountNumbers())
                               || request.getAccountNumbers().isEmpty()
                               || request.getAccountNumbers().contains(mca.getMembership().getCustomerAccount().getAccount().getAccountNo()))
                .peek(mta -> mta.setActive(false))
                .map(membershipTerminalAccessRepository::save)
                .map(MembershipTerminalAccessEntity::getMembership)
                .map(MembershipEntity::getCustomerAccount)
                .map(CustomerAccountEntity::getAccount)
                .map(AccountEntity::getAccountNo)
                .toList();
    }

    @Override
    public Membership updateMembershipTerminalAccessMaxWithdrawal(MembershipTerminalAccessWithdrawalLimitUpdateRequest request) {
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        Channel channel = channelService.findChannelByCode(request.getTerminalCode()).orElseThrow(() -> new NoMatchRecordFoundException("channelCode"));
        MembershipEntity membership = membershipRepository.findAccountMembershipByAccountNoAndUsername(request.getAccountNumber(), person.getUsername()).orElseThrow(() -> new NoMatchRecordFoundException("accountNumber"));
        MembershipTerminalAccessEntity mtaEntity = membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByMembership_IdAndChannelCode(membership.getId(), channel.getCode()).orElseThrow(() -> new NoMatchRecordFoundException("membershipCode"));
        mtaEntity.setMaxWithdrawalPerDay(BigDecimal.valueOf(Long.parseLong(request.getMaxWithdrawalPerDay())));
        MembershipTerminalAccessEntity saved = membershipTerminalAccessRepository.save(mtaEntity);
        return membershipMapper.toModel(saved.getMembership());
    }

    @Override
    @Transactional
    public List<Membership> syncMembershipList(CustomerSyncRequest request) {
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        AssetProvider assetProvider = assetProviderService.findAssetProviderById(Integer.parseInt(request.getAssetProviderId())).orElseThrow(() -> new InvalidInputException("assetProviderId"));
        List<Membership> localMemberships = findLocalMemberships(person, request.getAccountNumberList());
        List<ExternalAccountResponseData> remoteAccountList = findRemoteMemberships(assetProvider, person, assetProvider.getService().getCode(), null, null);
        // Analyzing memberships
        List<MembershipSync> membershipSyncList = createMembershipSyncList(localMemberships, remoteAccountList, person, assetProvider);
        return syncMemberships(membershipSyncList);
    }

    private List<MembershipSync> createMembershipSyncList(List<Membership> localMemberships,
                                                          List<ExternalAccountResponseData> remoteAccountList,
                                                          GeneralPerson person, AssetProvider assetProvider) {
        List<MembershipSync> membershipSyncList = new ArrayList<>();
        // Add all remote account with create status
        mapRemoteAccountToMembershipSync(membershipSyncList, remoteAccountList, assetProvider, person);
        // All local membership to added remote account and set status to update
        // If remote account does not any match for any local membership , remote account stay as create status
        // If local membership does not any match for any remote account , local membership added with delete status
        compareLocalMembershipWithMembershipSync(membershipSyncList, localMemberships, assetProvider, person);
        return membershipSyncList;
    }

    private void compareLocalMembershipWithMembershipSync(List<MembershipSync> membershipSyncList, List<Membership> localMemberships, AssetProvider assetProvider, GeneralPerson person) {
        localMemberships.forEach(localMembership -> {
            membershipSyncList
                    .stream()
                    .filter(membershipSync -> Objects.equals(Long.parseLong(localMembership.getCustomerAccount().getAccount().getAccountNo()), membershipSync.getAccountNumber()))
                    .findFirst()
                    .ifPresentOrElse(membershipSync -> {
                        membershipSync.setSyncStatus(MembershipSync.MembershipSyncStatus.UPDATED);
                        membershipSync.setLocalMembership(localMembership);
                    }, () -> {
                        MembershipSync membershipSync = new MembershipSync()
                                .setLocalMembership(localMembership)
                                .setAssetProvider(assetProvider)
                                .setPerson(person)
                                .setSyncStatus(DELETED);
                        membershipSyncList.add(membershipSync);
                    });
        });
    }

    private void mapRemoteAccountToMembershipSync(List<MembershipSync> membershipSyncList,
                                                  List<ExternalAccountResponseData> remoteAccountList,
                                                  AssetProvider assetProvider, GeneralPerson person) {
        remoteAccountList.forEach(remoteAccount -> {
            MembershipSync membershipSync = new MembershipSync()
                    .setRemoteAccount(remoteAccount)
                    .setAccountNumber(remoteAccount.getAccountNumber())
                    .setAssetProvider(assetProvider)
                    .setPerson(person)
                    .setSyncStatus(MembershipSync.MembershipSyncStatus.CREATED);
            membershipSyncList.add(membershipSync);
        });
    }

    public List<Membership> findLocalMemberships(GeneralPerson person, List<String> accountNumberList) {
        List<Membership> memberships = new ArrayList<>();
        membershipRepository
                .findAllByPersonUsername(person.getUsername())
                .stream()
                .filter(membership ->
                        Objects.isNull(accountNumberList) ||
                        accountNumberList.isEmpty() ||
                        accountNumberList.contains(membership.getCustomerAccount().getAccount().getAccountNo()))
                .map(membershipMapper::toModel)
                .forEach(memberships::add);
        return memberships;
    }

    private List<Membership> syncMemberships(List<MembershipSync> membershipSyncList) {
        return membershipSyncList
                .stream()
                .map(membershipSync -> switch (membershipSync.getSyncStatus()) {
                    case CREATED, UPDATED -> createOrUpdateMembership(membershipSync);
                    case DELETED -> deleteMembership(membershipSync);
                })
                .filter(Objects::nonNull)
                .toList();
    }


    private Membership deleteMembership(MembershipSync membershipSync) {
        //DELETE MEMBERSHIP LOGICALLY
        Membership localMembership = membershipSync.getLocalMembership();
        MembershipEntity entity = membershipRepository.findById(localMembership.getId()).orElseThrow(() -> new NoMatchRecordFoundException("localMembership"));
        // Deactivate membership channel access list
        membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByMembership_Id(entity.getId())
                .ifPresent(mca -> {
                    mca.getMembership().setClose(true);
                    membershipTerminalAccessRepository.save(mca);
                });
        // Deactivate account
        accountRepository
                .findByAccountNo(localMembership.getCustomerAccount().getAccount().getAccountNo())
                .ifPresent(accountEntity -> {
                    accountEntity.setClose(1);
                    accountRepository.save(accountEntity);
                });
        // Deactivate membership
        entity.setClose(true);
        membershipRepository.save(entity);
        return null;
    }


    private Membership createOrUpdateMembership(MembershipSync membershipSync) {
        AccountEntity accountEntity = syncAccount(membershipSync.getRemoteAccount(), membershipSync.getAssetProvider().getId());
        CustomerEntity customerEntity = syncCustomer(membershipSync.getRemoteAccount());
        CustomerAccountEntity customerAccountEntity = syncCustomerAccount(customerEntity, accountEntity, membershipSync.getRemoteAccount());
        MembershipEntity accountMembershipEntity = syncMembership(membershipSync.getPerson(), customerAccountEntity);
        return membershipMapper.toModel(accountMembershipEntity);
    }

    private MembershipEntity syncMembership(GeneralPerson person, CustomerAccountEntity customerAccountEntity) {
        Optional<MembershipEntity> membershipOptional = membershipRepository.findAccountMembershipByAccountNoAndUsername(customerAccountEntity.getAccount().getAccountNo(), person.getUsername());
        MembershipEntity accountMembership;
        if (membershipOptional.isEmpty()) {
            accountMembership = new MembershipEntity();
            accountMembership.setCustomerAccount(customerAccountEntity);
            accountMembership.setPerson(personRepository.findById(person.getId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId")));
            accountMembership.setArchiveNumber(ArchiveUtils.calculateOneMonthArchiveNo().intValue());
        } else {
            accountMembership = membershipOptional.get();
            accountMembership.setCustomerAccount(customerAccountEntity);
        }
        accountMembership.setClose(false);
        membershipRepository.save(accountMembership);
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

    private void validateAssetsFindRequest(MembershipLocalFindRequest findRequest) {
        String nationalId = findRequest.getNationalId();
        PersonType personType = findRequest.getPersonType();
        String subOrganizationId = findRequest.getSubOrganizationId();
        ValidationUtils.checkNull(personType, () -> new InvalidInputException("personType"));
        ValidationUtils.checkBlankStringIfNotNull(subOrganizationId, () -> new InvalidInputException("subOrganizationId"));
        ValidationUtils.checkBlankString(nationalId, () -> new InvalidInputException("nationalId"));
        checkPersonAssetAccess(findRequest.getNationalId());
    }

    private void checkPersonAssetAccess(String requestNationalId) {
        //TODO CHECK BY AUHTORITY
//        if (!isCustomerAdmin()) {
//            GeneralPerson currentPerson = getRequestCurrentPerson();
//            if (currentPerson instanceof GeneralLegalPerson legalPerson) {
//                String nationalId = legalPerson.getNationalId();
//                if (!requestNationalId.equals(nationalId)) {
//                    throw new InvalidInputException("nationalId");
//                }
//            } else if (currentPerson instanceof GeneralRealPerson realPerson) {
//                String nationalId = realPerson.getNationalCode();
//                if (!requestNationalId.equals(nationalId)) {
//                    throw new InvalidInputException("nationalId");
//                }
//            }
//        }
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
    public List<MembershipTerminalAccess> findMembershipChannelAccessList(Long personId, Short channelId) {
        if (null == personId) {
            return null;
        }
        Iterable<MembershipTerminalAccessEntity> membershipTerminalAccessEntities =
                membershipTerminalAccessRepository.findMembershipTerminalAccessEntitiesByPersonId(personId, channelId);

        return membershipTerminalAccessMapper.toMembershipTerminalAccessList(membershipTerminalAccessEntities);
    }

    @Override
    public List<MembershipTerminalAccessDto> findLocalMembershipTerminalAccesses(MembershipLocalFindRequest request) {
        if (null == request) {
            request = new MembershipLocalFindRequest();
        }
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        if (Objects.nonNull(request.getTerminal())) {
            TerminalType terminalCode = TerminalType.findByTerminalCode(request.getTerminal());
            return membershipTerminalAccessRepository
                    .findMembershipTerminalAccessEntitiesByPersonId(person.getId(), terminalCode.getLegacyTerminalId())
                    .stream()
                    .map(membershipTerminalAccessMapper::toMembershipTerminalAccess)
                    .map(membershipTerminalAccessMapper::toDto)
                    .toList();

        }
        throw new InvalidInputException("terminal");
    }

    @Override
    @Transactional
    public MembershipTerminalAccessDto editMembershipTerminalAccesses(MembershipLocalEditRequest request) {
        MembershipTerminalAccessEntity found = membershipTerminalAccessRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        found.setActive(request.getActive());
        found.setMaxPersWithdrawalPerDay(BigDecimal.valueOf(Long.parseLong(request.getMaxPersWithdrawalPerDay())));
        found.setMaxWithdrawalPerDay(BigDecimal.valueOf(Long.parseLong(request.getMaxWithdrawalPerDay())));
        found.setReason(request.getReason());
        found.setUserReason(request.getUserReason());
        MembershipTerminalAccessEntity saved = membershipTerminalAccessRepository.save(found);
        return membershipTerminalAccessMapper.toDto(saved);
    }

    @Override
    @Transactional
    public List<MembershipTerminalServiceAccessDto> membershipChannelAccessServiceAssignment(MembershipTerminalServiceAssignmentRequest request) {
        TerminalType terminalCode = TerminalType.findByTerminalCode(request.getTerminalCode());
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        MembershipTerminalAccessEntity mca = membershipTerminalAccessRepository
                .findMembershipTerminalAccessEntitiesByPersonIdAndAccountNo(person.getId(), terminalCode.getLegacyTerminalId(), request.getAccountNumber())
                .orElseThrow(() -> new NoMatchRecordFoundException("accountNumber"));
        List<MembershipTerminalServiceAccessEntity> mcsaList = membershipTerminalServiceAccessRepository
                .findByLegacyTerminalIdAndAccountNo(terminalCode.getLegacyTerminalId(), request.getAccountNumber(), person.getId())
                .stream()
                .toList();

        List<ChannelServiceAccessEntity> csaList = new ArrayList<>();
        request
                .getChannelServiceAccessIdList()
                .stream()
                .map(id -> channelServiceAccessRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("id:" + id)))
                .forEach(csaList::add);
        AssignmentType assignmentType = request.getAssignmentType();

        if (assignmentType.equals(AssignmentType.REVOKE)) {
            csaList
                    .stream()
                    .map(csa -> membershipTerminalServiceAccessRepository.findByChannelServiceAccessIdAndAccountNoAndPersonId(csa.getId(), request.getAccountNumber(),person.getId()))
                    .flatMap(Collection::stream)
                    .forEach(membershipTerminalServiceAccessRepository::delete);
        } else {
            csaList
                    .forEach(csa -> {
                        boolean hasBefore = mcsaList.stream().map(MembershipTerminalServiceAccessEntity::getChannelServiceAccess).anyMatch(f -> f.getId().equals(csa.getId()));
                        if (!hasBefore) {
                            MembershipTerminalServiceAccessEntity entity = new MembershipTerminalServiceAccessEntity();
                            entity.setChannelServiceAccess(csa);
                            entity.setMembershipTerminalAccess(mca);
                            entity.setArchiveNo(ArchiveUtils.calculateOneMonthArchiveNo().intValue()); //TODO
                            entity.setMaxWithdrawalPerTransaction(csa.getWithdrawalAmount());
                            membershipTerminalServiceAccessRepository.save(entity);
                        }
                    });

        }
        return findAllMembershipChannelServiceAccessList(new MembershipChannelServiceAccessFindRequest(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId(), terminalCode.getTerminalCode(), request.getAccountNumber()));
    }

    @Override
    public Optional<String> findCustomerNo(Long userId) {
        return membershipRepository.findMembershipListByUserId(userId)
                .stream().map(MembershipEntity::getCustomerAccount)
                .filter(Objects::nonNull)
                .map(CustomerAccountEntity::getCustomer)
                .filter(Objects::nonNull)
                .map(CustomerEntity::getCustomerNo)
                .findFirst();
    }


    @Override
    @LegacyChannelManger
    public List<MembershipTerminalServiceAccessDto> findAllMembershipChannelServiceAccessList(MembershipChannelServiceAccessFindRequest request) {
        GeneralPerson person = personService.findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrganizationId()).orElseThrow(() -> new NoMatchRecordFoundException("nationalId"));
        TerminalType terminalCode = TerminalType.findByTerminalCode(request.getTerminalCode());
        return membershipTerminalServiceAccessRepository
                .findByLegacyTerminalIdAndAccountNo(terminalCode.getLegacyTerminalId(), request.getAccountNumber(), person.getId())
                .stream()
                .map(membershipTerminalServiceAccessMapper::toDto)
                .toList();
    }

    @Override
    public MembershipTerminalAccessDto getMembershipChannelAccess(long id) {
        MembershipTerminalAccessEntity found = membershipTerminalAccessRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        return membershipTerminalAccessMapper.toDto(found);
    }

    @Override
    @LegacyChannelManger
    public List<ServiceCategory> findAllServiceCategory() {
        if (DEFAULT_SERVICE_CATEGORY_CACHE.isEmpty()) {
            jdbcTemplate.query("select * from REF.SERVICE_CATEGORY"
                    , (rs, rowNum) -> new ServiceCategory()
                            .setServiceCategoryId(rs.getInt("SERVICE_CATEGORY_ID"))
                            .setName(StringUtils.trim(rs.getString("NAME")).toString())
                            .setDescription(StringUtils.trim(rs.getString("DESCRIPTION")).toString())).forEach(serviceCategory -> {
                DEFAULT_SERVICE_CATEGORY_CACHE.put(serviceCategory.getServiceCategoryId(), serviceCategory);
            });
        }
        return new ArrayList<>(DEFAULT_SERVICE_CATEGORY_CACHE.values());
    }

    @LegacyChannelManger
    private LegacyTerminalDetail findLegacyTerminalDetail(long legacyTerminalId) {
        return jdbcTemplate.query("select * from REF.CHANNEL where CHANNEL_ID = ?",
                        (rs, rowNum) -> new LegacyTerminalDetail()
                                .setMaxWithdrawalPerDay(rs.getBigDecimal("MAX_WITHDRAWAL_PER_DAY")),
                        legacyTerminalId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoMatchRecordFoundException("legacyTerminalId"));
    }

    @Data
    @Accessors(chain = true)
    @LegacyChannelManger
    private static class LegacyTerminalDetail {
        private BigDecimal maxWithdrawalPerDay;

    }

    @Data
    @Accessors(chain = true)
    @LegacyChannelManger
    public static class LegacyChannelServiceAccess {
        private Long channelServiceAccessId;
        private BigDecimal withdrawalAmount;
    }

}
