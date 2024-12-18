package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.Scope;
import ir.daneshrefah.scm.uaa.mapper.ClientMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.entity.ClientEntity;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientCreateRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientEditRequest;
import ir.daneshrefah.scm.uaa.service.client.dto.ClientFindRequest;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClientService {

    private final List<Client> CLIENT_LIST = new ArrayList<>();

    private final ClientScopeService scopeService;
    private final ClientScopeRelationService scopeRelationService;
    private final ClientAuthorizationGrantTypeService authorizationGrantTypeService;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;


    public Scope findScopeByCode(String code) {
        return scopeService.findByCode(code).orElse(null);
    }


    @PostConstruct
    public void init() {
        reloadCache();
    }

    public void reloadCache() {
        synchronized (CLIENT_LIST) {
            CLIENT_LIST.clear();
            clientRepository
                    .findAll()
                    .stream()
                    .map(ClientMapper.INSTANCE::toModel)
                    .peek(client -> {
                        client.setScopes(scopeRelationService.findClientScopeRelation(client.getId()));
                    })
                    .forEach(CLIENT_LIST::add);
        }
    }

    public List<Client> findAll() {
        return CLIENT_LIST;
    }

    public PagedResponseData<Client> findPagedClientList(ClientFindRequest request) {
        List<Client> clientList = findAll().stream()
                .filter(client -> Objects.isNull(request.getId()) || client.getId().equals(request.getId()))
                .filter(client -> StringUtils.isBlank(request.getClientId()) || client.getClientId().equals(request.getClientId()))
                .filter(client -> StringUtils.isBlank(request.getTerminalCode()) || client.getTerminalCode().equals(request.getTerminalCode()))
                .filter(client -> StringUtils.isBlank(request.getTitle()) || StringUtils.containsIgnoreCase(client.getTitle(), request.getTitle()))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, clientList);
    }

    public Optional<Client> findByClientId(String clientId) {
        return findAll().stream().filter(client -> clientId.equalsIgnoreCase(client.getClientId())).findFirst();
    }

    public Optional<Client> findById(Long id) {
        return findAll().stream().filter(client -> Objects.equals(id, client.getId())).findFirst();
    }

    public Client save(Client client) {
        ClientEntity entity = ClientMapper.INSTANCE.toClientIdEntity(client);
        if (Objects.nonNull(entity.getVersions())) {
            entity.getVersions().forEach(clientVersionEntity -> clientVersionEntity.setClient(entity));
        }
        ClientEntity save = clientRepository.save(entity);
        client = ClientMapper.INSTANCE.toModel(save);
        reloadCache();
        return client;
    }

    @Transactional
    public Client updateClient(ClientEditRequest request) {
        ClientEntity foundEntity = clientRepository.findById(request.getId()).orElseThrow(() -> new NoMatchRecordFoundException("id"));
        ClientEntity entity = ClientMapper.INSTANCE.toEntity(mapClientEditRequestToClient(request));
        dynamicMap(entity, foundEntity);
        ClientEntity saved = clientRepository.save(foundEntity);
        updateAuthGrantType(saved, request);
        reloadCache();
        return findByClientId(request.getClientId()).orElseThrow();
    }

    private void updateAuthGrantType(ClientEntity saved, ClientEditRequest request) {
        saved.getAuthorizationGrantTypes().forEach(dbAuthGrant -> {
            authorizationGrantTypeService.revokeGrantType(dbAuthGrant.getAuthorizationGrantType(), saved.getId());
        });
        request.getAuthorizationGrantTypes().forEach(reqAuthGrant -> {
            authorizationGrantTypeService.assignGrantType(reqAuthGrant, saved.getId());
        });
    }


    private void dynamicMap(ClientEntity entity, ClientEntity dbEntity) {
        dbEntity.setTitle(entity.getTitle());
        dbEntity.setClientId(entity.getClientId());
        dbEntity.setClientSecret(entity.getClientSecret());
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
        dbEntity.setAuthorizationGrantTypes(entity.getAuthorizationGrantTypes());
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
    public Client remove(Long clientId, LocalDateTime lastEditDate) {
        ClientEntity entity = clientRepository.findById(clientId).orElseThrow(() -> new NoMatchRecordFoundException("clientId"));
        entity.setLastEditDate(lastEditDate);
        clientRepository.delete(entity);
        reloadCache();
        return ClientMapper.INSTANCE.toModel(entity);
    }

    @Transactional
    public Client create(ClientCreateRequest request) {
        Client client = mapClientCreateRequestToClient(request);
        ClientEntity entity = ClientMapper.INSTANCE.toEntity(client);
        ClientEntity saved = clientRepository.save(entity);
        client
                .getAuthorizationGrantTypes()
                .forEach(authGrantType -> {
                    authorizationGrantTypeService.assignGrantType(authGrantType, saved.getId());
                });
        reloadCache();
        return findByClientId(saved.getClientId()).orElseThrow();
    }

    private Client mapClientEditRequestToClient(ClientEditRequest request) {
        Client client = mapClientCreateRequestToClient(request);
        client.setId(request.getId());
        client.setLastEditDate(LocalDateTime.now());
        return client;
    }

    private Client mapClientCreateRequestToClient(ClientCreateRequest request) {
        Client client = new Client();
        client.setTitle(request.getTitle());
        client.setClientId(request.getClientId());
        client.setClientSecret(request.getClientSecret());
        client.setTerminalCode(request.getTerminalCode());
        client.setAuthenticationMethods(request.getAuthenticationMethods());
        client.setRedirectUris(request.getRedirectUris());
        client.setRequireAuthorizationConsent(request.getRequireAuthorizationConsent());
        client.setRequireProofKey(request.getRequireProofKey());
        client.setCheckVersion(request.getCheckVersion());
        client.setCheckActivation(request.getCheckActivation());
        client.setSessionTimeToLiveMinute(Long.parseLong(request.getSessionTimeToLiveMinute()));
        client.setAuthorizationGrantTypes(request.getAuthorizationGrantTypes());
        client.setCheckIpAddress(request.getCheckIpAddress());
        client.setAllowIpAddresses(request.getAllowIpAddresses());
        client.setScopes(null);
        client.setVersions(null);
        return client;
    }
}
