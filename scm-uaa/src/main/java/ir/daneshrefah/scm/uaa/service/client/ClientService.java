package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.data.entity.person.*;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.repository.PersonRepository;
import ir.daneshrefah.scm.common.data.service.person.PersonService;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.person.*;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.model.user.UserType;
import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.controller.user.PasswordModificationRequest;
import ir.daneshrefah.scm.uaa.controller.user.UserDataChangeRequest;
import ir.daneshrefah.scm.uaa.controller.user.UserDataRequest;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.mapper.ClientMapper;
import ir.daneshrefah.scm.uaa.mapper.UserMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientAuthorizationGrantTypeEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientCreateRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientEditRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientFindRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClientService {

    private static final AtomicBoolean DIRTY_CACHE = new AtomicBoolean(true);
    private final List<Client> CLIENT_LIST = new ArrayList<>();
    private final ClientScopeService scopeService;
    private final ClientScopeRelationService scopeRelationService;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;
    private final PersonService personService;
    private final UserService userService;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;
    private final ClientMapper clientMapper;
    private final UserMapper userMapper;

    @Value("${scm.security.client.default-role:#{null}}")
    private String defaultClientRoleCode;

    public Scope findScopeByCode(String code) {
        return scopeService.findByCode(code).orElse(null);
    }

    @PostConstruct
    public void init() {
        findAll();
    }

    public void reloadCache() {
        DIRTY_CACHE.set(true);
    }

    public List<Client> findAll() {
        if (DIRTY_CACHE.getAndSet(false) || CLIENT_LIST.isEmpty()) {
            synchronized (CLIENT_LIST) {
                CLIENT_LIST.clear();
                clientRepository
                        .findAll()
                        .stream()
                        .filter(client -> Objects.equals(client.getStatus(), true))
                        .map(clientMapper::toModel)
                        .peek(client -> {
                            client.setScopes(scopeRelationService.findClientScopeRelation(client.getId()));
                        })
                        .forEach(CLIENT_LIST::add);
            }
        }
        return CLIENT_LIST;
    }

    public PagedResponseData<ClientResponse> findPagedClientList(ClientFindRequest request) {
        List<ClientResponse> clientList = findAll().stream()
                .filter(client -> Objects.isNull(request.getId()) || client.getId().equals(request.getId()))
                .filter(client -> StringUtils.isBlank(request.getNickname()) || client.getUser().getNickname().equals(request.getNickname()))
                .filter(client -> StringUtils.isBlank(request.getTerminalCode()) || client.getTerminalCode().equals(request.getTerminalCode()))
                .filter(client -> StringUtils.isBlank(request.getTitle()) || StringUtils.containsIgnoreCase(client.getUser().getPerson().getTitle(), request.getTitle()))
                .map(clientMapper::toResponse)
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, clientList);
    }

    public Optional<Client> findByNickname(String nickname) {
        return findAll().stream().filter(client -> nickname.equalsIgnoreCase(client.getUser().getNickname())).findFirst();
    }

    public Optional<Client> findById(Long id) {
        return findAll().stream().filter(client -> Objects.equals(id, client.getId())).findFirst();
    }

    public Optional<ClientResponse> getClientResponseById(Long id) {
        return findAll().stream().filter(client -> Objects.equals(id, client.getId())).map(clientMapper::toResponse).findFirst();
    }

    public Client save(Client client) {
        ClientEntity entity = clientMapper.toEntity(client);
        if (Objects.nonNull(entity.getVersions())) {
            entity.getVersions().forEach(clientVersionEntity -> clientVersionEntity.setClient(entity));
        }
        ClientEntity save = clientRepository.save(entity);
        client = clientMapper.toModel(save);
        reloadCache();
        return client;
    }

    @Transactional
    public ClientResponse updateClient(ClientEditRequest request) {
        ClientEntity foundEntity = clientRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        ClientEntity entity = clientMapper.toEntity(mapClientEditRequestToClient(request));
        updateAuthGrantType(foundEntity, request);
        dynamicMap(entity, foundEntity);
        if (StringUtils.isNotBlank(request.getPassword())) {
            updateClientPassword(request);
        }
        if (StringUtils.isNotBlank(request.getNickname()) && !request.getNickname().equals(foundEntity.getUser().getNickname())) {
            updateClientNickName(request, foundEntity);
        }
        updateClientPerson(request, foundEntity);
        clientRepository.save(foundEntity);
        reloadCache();
        return clientMapper.toResponse(findByNickname(request.getNickname()).orElseThrow());
    }

    private void updateClientPerson(ClientEditRequest request, ClientEntity foundEntity) {
        String title = request.getTitle();
        String titleFa = request.getTitleFa();
        if (!Objects.equals(titleFa, ((GeneralLegalPersonEntity) foundEntity.getUser().getPerson()).getTitle())
            || !Objects.equals(title, ((GeneralLegalPersonEntity) foundEntity.getUser().getPerson()).getTitleEnglish())) {
            personRepository.findById(foundEntity.getUser().getPerson().getId()).ifPresent(person -> {
                GeneralLegalPersonEntity clientPerson = (GeneralLegalPersonEntity) person;
                clientPerson.setTitle(titleFa);
                clientPerson.setTitleEnglish(title);
                personRepository.save(person);
            });
        }
    }

    private void updateClientNickName(ClientEditRequest request, ClientEntity entity) {
        UserDataChangeRequest userDataChangeRequest = new UserDataChangeRequest();
        userDataChangeRequest.setNickname(request.getNickname());
        userDataChangeRequest.setId(entity.getUser().getId());
        userService.changeUser(userDataChangeRequest);
    }

    private void updateClientPassword(ClientEditRequest request) {
        PasswordModificationRequest changePasswordRequest = new PasswordModificationRequest();
        changePasswordRequest.setUsername(request.getNickname());
        changePasswordRequest.setTerminalCode(request.getTerminalCode());
        changePasswordRequest.setOldPassword(StringUtils.EMPTY);
        changePasswordRequest.setNewPassword(request.getPassword());
        userService.updateUserLoginStaticPassword(changePasswordRequest, true);
    }

    private void updateAuthGrantType(ClientEntity entity, ClientEditRequest request) {
        Set<ClientAuthorizationGrantTypeEntity> combine = new HashSet<>();
        Set<ClientAuthorizationGrantTypeEntity> grantTypeEntities = entity.getAuthorizationGrantTypes();
        if (Objects.isNull(grantTypeEntities)) {
            grantTypeEntities = new HashSet<>();
        }
        //comparing
        for (AuthorizationGrantType authorizationGrantType : request.getAuthorizationGrantTypes()) {
            combine.add(grantTypeEntities
                    .stream()
                    .filter(e -> e.getAuthorizationGrantType().equals(authorizationGrantType))
                    .findFirst()
                    .orElseGet(() -> {
                        ClientAuthorizationGrantTypeEntity grantTypeEntity = new ClientAuthorizationGrantTypeEntity();
                        grantTypeEntity.setAuthorizationGrantType(authorizationGrantType);
                        grantTypeEntity.setClient(entity);
                        return grantTypeEntity;
                    }));
        }
        grantTypeEntities.clear();
        grantTypeEntities.addAll(combine);
    }


    private void dynamicMap(ClientEntity entity, ClientEntity dbEntity) {
        dbEntity.setTerminalCode(entity.getTerminalCode());
        dbEntity.setClientAuthenticationMethodSecretBasic(entity.isClientAuthenticationMethodSecretBasic());
        dbEntity.setClientAuthenticationMethodSecretPost(entity.isClientAuthenticationMethodSecretPost());
        dbEntity.setClientAuthenticationMethodSecretJwt(entity.isClientAuthenticationMethodSecretJwt());
        dbEntity.setClientAuthenticationMethodKeyJwt(entity.isClientAuthenticationMethodKeyJwt());
        dbEntity.setClientAuthenticationMethodNone(entity.isClientAuthenticationMethodNone());
        dbEntity.setRedirectUris(entity.getRedirectUris());
        dbEntity.setRequireAuthorizationConsent(entity.isRequireAuthorizationConsent());
        dbEntity.setRequireProofKey(entity.isRequireProofKey());
        dbEntity.setCheckVersion(entity.isCheckVersion());
        dbEntity.setCheckActivation(entity.isCheckActivation());
        dbEntity.setSessionTimeToLiveMinute(entity.getSessionTimeToLiveMinute());
        dbEntity.setCheckIpAddress(entity.isCheckIpAddress());
        dbEntity.setAllowIpAddresses(entity.getAllowIpAddresses());
        dbEntity.setLastEditDate(LocalDateTime.now());
    }

    //    @Cacheable(cacheNames = "CACHE_CLIENT_AUTHORITY", key = "#clientId")
    public Optional<List<String>> loadClientAuthorities(Long clientId) {
        List<RoleEntity> roles = roleRepository.findByClientId(clientId);
        if (null == roles || roles.isEmpty())
            return Optional.empty();
        return Optional.of(roles.stream()
                .map(RoleEntity::getCode)
                .collect(Collectors.toList()));
    }

    @Transactional
    public ClientResponse remove(Long clientId, LocalDateTime lastEditDate) {
        ClientEntity entity = clientRepository.findById(clientId).orElseThrow(() -> new NoMatchRecordFoundException("clientId"));
        entity.setLastEditDate(lastEditDate);
        entity.setStatus(false);
        clientRepository.save(entity);
        reloadCache();
        return clientMapper.toResponse(clientMapper.toModel(entity));
    }

    @Transactional
    public ClientResponse create(ClientCreateRequest request) {
        validateClientCreateRequest(request);
        Client client = clientMapper.toModel(request);
        ClientEntity entity = clientMapper.toEntity(client);
        Set<ClientAuthorizationGrantTypeEntity> grantTypes = new HashSet<>();
        request
                .getAuthorizationGrantTypes()
                .forEach(authGrantType -> {
                    ClientAuthorizationGrantTypeEntity grantEntity = new ClientAuthorizationGrantTypeEntity();
                    grantEntity.setClient(entity);
                    grantEntity.setAuthorizationGrantType(authGrantType);
                    grantTypes.add(grantEntity);
                });
        entity.setAuthorizationGrantTypes(grantTypes);
        GeneralLegalPerson legalPerson = findOrCreateLegalPerson(request);
        ClientPerson clientPerson = createClientPerson(request);
        User clientUser = createClientUser(request, clientPerson);
        checkClientRoleAssignment(clientPerson);
        entity.setUser(userMapper.toEntity(clientUser));
        entity.setStatus(true);
        entity.setLegalPerson((GeneralLegalPersonEntity) personMapper.toPersonEntity(legalPerson));
        ClientEntity saved = clientRepository.save(entity);
        reloadCache();
        return clientMapper.toResponse(findByNickname(saved.getUser().getNickname()).orElseThrow());
    }

    private ClientPerson createClientPerson(ClientCreateRequest request) {
        GeneralLegalPersonEntity clientPerson = new ClientPersonEntity();
        String clientPersonSubOrg = getClientPersonSubOrg(request.getNationalId());
        clientPerson.setSubOrganizationId(clientPersonSubOrg);
        clientPerson.setTitleEnglish(request.getTitle());
        clientPerson.setTitle(request.getTitle());
        clientPerson.setNationality(Nationality.IRANIAN);
        clientPerson.setUsername(request.getNickname());
        clientPerson.setRegisterIssueDate(LocalDate.now());
        clientPerson.setNationalId(request.getNationalId());
        clientPerson.setStatus(PersonStatus.ACTIVE);
        clientPerson.setPostalCode1(StringUtils.EMPTY);
        clientPerson.setArchiveNo(8L); // DATABASE DEFAULT IS 8 //TODO
        clientPerson.setIdentificationNo(StringUtils.EMPTY);
        clientPerson.setIssuePlace(StringUtils.EMPTY);
        clientPerson.setFatherName(StringUtils.EMPTY);
        clientPerson.setIdentificationSerial(StringUtils.EMPTY);
        clientPerson.setGender(Gender.MALE);
        clientPerson.setLastName(StringUtils.EMPTY);
        return (ClientPerson) personMapper.toPerson(personRepository.save(clientPerson));
    }

    private String getClientPersonSubOrg(String nationalId) {
        return personService
                .findAllClientPerson(nationalId)
                .stream()
                .map(ClientPerson::getSubOrganizationId)
                .map(Integer::parseInt)
                .max(Integer::compareTo)
                .map(o -> o + 1)
                .map(n -> String.format("%08d", n))
                .orElse("00000000");
    }

    private void validateClientCreateRequest(ClientCreateRequest request) {
        PersonType personType = request.getPersonType();
        Set<PersonType> notAcceptableTypes = Set.of(
                PersonType.REAL,
                PersonType.UNKNOWN,
                PersonType.EMPLOYEE
        );
        if (notAcceptableTypes.contains(personType)) {
            throw new InvalidInputException("personType");
        }
    }

    private User createClientUser(ClientCreateRequest request, GeneralLegalPerson legalPerson) {
        UserDataRequest userDataRequest = new UserDataRequest();
        userDataRequest.setNickname(request.getNickname());
        userDataRequest.setTerminalCode(request.getTerminalCode().toUpperCase());
        String password = StringUtils.isNotBlank(request.getPassword()) ? request.getPassword() : "myClientSecret";
        userDataRequest.setCreatorBranch(StringUtils.EMPTY);
        userDataRequest.setLoginStaticPassword(password);
        userDataRequest.setPersonId(legalPerson.getId());
        userDataRequest.setTransactionStaticPassword(password);
        userDataRequest.setUserType(UserType.CM_CLIENT);
        userDataRequest.setLoginAuthenticationMethod(AuthenticationMethod.STATIC_PASSWORD);
        userDataRequest.setTransactionAuthenticationMethod(AuthenticationMethod.STATIC_PASSWORD);
        return userService.createUser(userDataRequest);
    }

    private GeneralLegalPerson findOrCreateLegalPerson(ClientCreateRequest request) {
        GeneralLegalPerson dbPerson = personService
                .findPerson(request.getPersonType(), request.getNationalId(), request.getSubOrg())
                .map(GeneralLegalPerson.class::cast)
                .orElseGet(() -> {
                    GeneralLegalPersonEntity person =
                            switch (request.getPersonType()) {
                                case CORPORATE -> new CorporatePersonEntity();
                                case BANK -> new BankPersonEntity();
                                case GOVERNANCE -> new GovernancePersonEntity();
                                case TAMIN -> new TaminPersonEntity();
                                default -> throw new InvalidInputException("personType");
                            };
                    person.setTitle(request.getTitle());
                    person.setTitleEnglish(request.getTitle());
                    person.setUsername(request.getNickname());
                    person.setNationality(Nationality.IRANIAN);
                    person.setRegisterIssueDate(LocalDate.now());
                    person.setStatus(PersonStatus.ACTIVE);
                    person.setNationalId(request.getNationalId());
                    person.setArchiveNo(8L); // DATABASE DEFAULT IS 8
                    person.setPostalCode1(StringUtils.EMPTY);
                    person.setIdentificationNo(StringUtils.EMPTY);
                    person.setIssuePlace(StringUtils.EMPTY);
                    person.setFatherName(StringUtils.EMPTY);
                    person.setIdentificationSerial(StringUtils.EMPTY);
                    person.setGender(Gender.MALE);
                    person.setLastName(StringUtils.EMPTY);
                    return (GeneralLegalPerson) personMapper.toPerson(personRepository.save(person));
                });
        return dbPerson;
    }

    private void checkClientRoleAssignment(ClientPerson dbPerson) {
        if (StringUtils.isBlank(defaultClientRoleCode)) {
            throw new IllegalArgumentException("defaultClientRoleCode");
        }
        boolean hasClientRole = roleRepository
                .findByPersonId(dbPerson.getId())
                .stream()
                .map(RoleEntity::getCode)
                .anyMatch(defaultClientRoleCode::equalsIgnoreCase);
        if (!hasClientRole) {
            RoleEntity clientRoleEntity = roleRepository.findByCode(defaultClientRoleCode.toUpperCase()).orElseThrow(() -> new NoMatchRecordFoundException("clientRole"));
            roleRepository.insertPersonRole(dbPerson.getId(), clientRoleEntity.getId());
        }
    }

    private Client mapClientEditRequestToClient(ClientEditRequest request) {
        Client client = clientMapper.toModel(request);
        client.setId(request.getId());
        client.setLastEditDate(LocalDateTime.now());
        return client;
    }


}
