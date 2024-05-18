package ir.daneshrefah.scm.uaa.service.client;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.*;
import ir.daneshrefah.scm.uaa.mapper.ClientMapper;
import ir.daneshrefah.scm.uaa.mapper.ScopeMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.RoleRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ScopeRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ClientService {

    private List<Scope> scopeList = null;
    private List<Client> clientList = null;

    private final ScopeRepository scopeRepository;
    private final ClientRepository clientRepository;
    private final RoleRepository roleRepository;

    public List<Scope> findScopeList() {
        if (null == scopeList) {
            scopeList = ScopeMapper.INSTANCE.toModels(scopeRepository.findAll());
        }
        return scopeList;
    }

    public Scope findScopeByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        return findScopeList().stream().filter(scope -> code.equalsIgnoreCase(scope.getCode()))
                .findFirst()
                .orElse(null);
    }

    public List<Client> findAll() {
        if (null == clientList) {
            clientList = ClientMapper.INSTANCE.toModels(clientRepository.findAll());
        }
        return clientList;
    }

    public Optional<Client> findByClientId(String clientId) {
        return findAll().stream().filter(client -> clientId.equalsIgnoreCase(client.getClientId())).findFirst();
    }

    public Optional<Client> findById(Long id) {
        return findAll().stream().filter(client -> id == client.getId()).findFirst();
    }

    public Client save(Client client) {
        ClientEntity entity = ClientMapper.INSTANCE.toClientIdEntity(client);
        if (Objects.nonNull(entity.getVersions())) {
            entity.getVersions().forEach(clientVersionEntity -> clientVersionEntity.setClient(entity));
        }
        ClientEntity save = clientRepository.save(entity);
        client = ClientMapper.INSTANCE.toModel(save);
        return client;
    }

    public Client updateClient(Client client) {
//        ClientEntity entity = ClientMapper.INSTANCE.toEntity(client);
//        client = ClientMapper.INSTANCE.toModel(clientRepository.save(entity));
        return client;
    }

//    @Cacheable(cacheNames = "CACHE_CLIENT_AUTHORITY", key = "#clientId")
    public Optional<List<String>> loadClientAuthorities(Long clientId) {
        List<RoleEntity> roles = roleRepository.findByClientId(clientId);
        if (null == roles || roles.isEmpty())
            return Optional.empty();
        return Optional.of(roles.stream()
                .map(r -> r.getCode())
                .collect(Collectors.toList()));
    }

    private Client prepareMockClient() {
//        createClient(null);
//        Iterable<ClientEntity> clientEntities = clientRepository.findAll();
        Client client = new Client();
        client.setId(9876L);
        client.setTitle("Mobile Bank");
        client.setClientId("ib");
        client.setClientSecret("{noop}myClientSecretValue");
        client.setAuthenticationMethods((Arrays.asList(ClientAuthenticationMethod.CLIENT_SECRET_BASIC,
                ClientAuthenticationMethod.CLIENT_SECRET_POST)));
        client.setAuthorizationGrantTypes(Arrays.asList(AuthorizationGrantType.AUTHORIZATION_CODE,
                /*AuthorizationGrantType.REFRESH_TOKEN, */AuthorizationGrantType.CLIENT_CREDENTIALS,
                AuthorizationGrantType.FIRST_PASSWORD, AuthorizationGrantType.SECOND_PASSWORD));
        client.setRedirectUris(Arrays.asList("http://127.0.0.1:8080/login/oauth2/code/users-client-oidc",
                "http://127.0.0.1:8080/authorized"));
        client.setTerminalCode("IB");
//        client.setScopes(Arrays.asList(OidcScopes.OPENID, OidcScopes.PROFILE));
        client.setRequireAuthorizationConsent(true);
        client.setRequireClientAuthentication(false);
        client.setCheckVersion(true);
        client.setCheckActivation(true);
        client.setSessionTimeToLiveMinute(1000L);
        /*client.setVersions(Arrays.asList(
                new ClientVersion("MB-3.3.4", false,
                        "DF2A4EB3A644FE1F43DFBD9D818991B8262AD45982D5A9BD81A1D5CDB0EA0A0A132ADF9AC3097E07734942817A0A6CE32155F106C6D613999412A266B0A6B0A4",
                        ClientVersionStatus.VALID)));*/
        return client;
    }

}
