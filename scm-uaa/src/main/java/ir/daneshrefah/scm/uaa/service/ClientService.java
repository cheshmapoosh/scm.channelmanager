package ir.daneshrefah.scm.uaa.service;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.*;
import ir.daneshrefah.scm.uaa.mapper.ClientMapper;
import ir.daneshrefah.scm.uaa.mapper.ScopeMapper;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ClientRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.client.ScopeRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ClientService {

    private List<Scope> scopeList = null;
    private List<Client> clientList = null;

    private final ScopeRepository scopeRepository;
    private final ClientRepository clientRepository;

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

    public Client findByClientId(String clientId) {
        return findAll().stream().filter(client -> clientId.equalsIgnoreCase(client.getClientId())).findFirst().orElseThrow();
    }

    public Client createClient(Client client) {
        client = new Client();
        client.setTitle("Internet Bank");
        client.setClientId("IB");
        client.setClientSecret("{noop}myClientSecretValue");
        client.setTerminalCode("IB");
        client.setAuthenticationMethods(Arrays.asList(ClientAuthenticationMethod.CLIENT_SECRET_POST));
        client.setAuthorizationGrantTypes(Arrays.asList(AuthorizationGrantType.AUTHORIZATION_CODE,
                AuthorizationGrantType.CLIENT_CREDENTIALS,
                AuthorizationGrantType.FIRST_PASSWORD,
                AuthorizationGrantType.SECOND_PASSWORD));
        client.setRedirectUris(Arrays.asList("http://127.0.0.1:8080/login/oauth2/code/users-client-oidc",
                "http://127.0.0.1:8080/authorized"));
        client.setRequireAuthorizationConsent(true);
        client.setRequireClientAuthentication(false);
        client.setRequireProofKey(false);
        client.setCheckVersion(true);
        client.setCheckActivation(true);
        client.setSessionTimeToLiveMinute(1000L);

        ClientVersion clientVersion = new ClientVersion();
        clientVersion.setVersion("MB-3.3.7");
        clientVersion.setForced(false);
        clientVersion.setSignature("DF2A4EB3A644FE1F43DFBD9D818991B8262AD45982D5A9BD81A1D5CDB0EA0A0A132ADF9AC3097E07734942817A0A6CE32155F106C6D613999412A266B0A6B0A4-2825155330-4259616679");
        clientVersion.setStatus(ClientVersionStatus.VALID);
        client.setVersions(Arrays.asList(clientVersion));

        ClientScopeRelation clientScopeRelation = new ClientScopeRelation();
        clientScopeRelation.setClient(client);
        clientScopeRelation.setScope(findScopeByCode("session"));
        clientScopeRelation.setCreator("Reza Jamshidi");
        clientScopeRelation.setLastEditor("Reza Jamshidi");
        client.setScopes(Arrays.asList(clientScopeRelation));

//        ClientEntity entity = ClientMapper.INSTANCE.toEntity(client);
//        client = ClientMapper.INSTANCE.toModel(clientRepository.save(entity));
        return client;
    }

    public Client updateClient(Client client) {
//        ClientEntity entity = ClientMapper.INSTANCE.toEntity(client);
//        client = ClientMapper.INSTANCE.toModel(clientRepository.save(entity));
        return client;
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
