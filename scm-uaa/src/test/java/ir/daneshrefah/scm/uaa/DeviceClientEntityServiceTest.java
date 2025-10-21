package ir.daneshrefah.scm.uaa;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.domain.client.*;
import ir.daneshrefah.scm.uaa.service.client.ClientScopeService;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
//import ir.daneshrefah.scm.uaa.service.client.ClientVersionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class DeviceClientEntityServiceTest {
    @Autowired
    private ClientService clientService;
//    @Autowired
//    private ClientVersionService clientVersionService;
    @Test
    public void testClientSave(){
        Client client = new Client();
//        client.setTitle("Internet Bank");
//        client.setClientId("IB");
//        client.setClientSecret("{noop}myClientSecretValue");
        client.setTerminalCode("IB");
        client.setAuthenticationMethods(Arrays.asList(ClientAuthenticationMethod.CLIENT_SECRET_POST));
//        client.setClientAuthorizationGrantTypes(Arrays.asList(AuthorizationGrantType.AUTHORIZATION_CODE,
//                AuthorizationGrantType.CLIENT_CREDENTIALS,
//                AuthorizationGrantType.FIRST_PASSWORD,
//                AuthorizationGrantType.SECOND_PASSWORD));
        client.setRedirectUris(Arrays.asList("http://127.0.0.1:8080/login/oauth2/code/users-client-oidc",
                "http://127.0.0.1:8080/authorized"));
        client.setRequireAuthorizationConsent(true);
        client.setRequireProofKey(false);
        client.setCheckVersion(true);
        client.setCheckActivation(true);
        client.setSessionTimeToLiveMinute(1000L);

        ClientVersion byId = new ClientVersion();
        byId.setVersion("MB-3.3.7");
        byId.setForced(false);
        byId.setSignature("DF2A4EB3A644FE1F43DFBD9D818991B8262AD45982D5A9BD81A1D5CDB0EA0A0A132ADF9AC3097E07734942817A0A6CE32155F106C6D613999412A266B0A6B0A4-2825155330-4259616679");
        byId.setStatus(ClientVersionStatus.VALID);



//        Optional<ClientVersion> byId = clientVersionService.findById(1L);
        client.setVersions(List.of(byId));

        ClientScopeRelation clientScopeRelation = new ClientScopeRelation();
        clientScopeRelation.setClient(client);
        clientScopeRelation.setScope(clientService.findScopeByCode("session"));
//        clientScopeRelation.setCreator("Reza Jamshidi");
//        clientScopeRelation.setLastEditor("Reza Jamshidi");
        client.setScopes(List.of(clientScopeRelation));
        Client saved = clientService.save(client);
        System.out.println(saved);
    }


}
