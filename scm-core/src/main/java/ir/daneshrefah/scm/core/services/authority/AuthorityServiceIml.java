package ir.daneshrefah.scm.core.services.authority;

import ir.daneshrefah.scm.common.service.authority.AuthorityService;
import ir.daneshrefah.scm.core.authority.decision.configuration.handler.AuthorizationManagerDecisionChain;
import ir.daneshrefah.scm.core.authority.decision.configuration.model.AuthoritiesSecurityContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorityServiceIml implements AuthorityService {

    private final List<AuthorizationManagerDecisionChain> authorizationManagerDecisionChains;
    private final List<AuthorizationManager<AuthoritiesSecurityContext>> authorizationManagers;
    private List<String> authorizationManagerDecisionChainsNames;
    private List<String> authorizationManagerName;

    @PostConstruct
    public void init() {
        authorizationManagerDecisionChainsNames = authorizationManagerDecisionChains
                .stream()
                .filter(authorizationManagerDecisionChain ->
                        authorizationManagerDecisionChain.getClass().getName().startsWith("ir.daneshrefah"))
                .map(authorizationManagerDecisionChain ->
                        authorizationManagerDecisionChain.getClass().getSimpleName())
                .toList();
        authorizationManagerName = authorizationManagers.stream()
                .filter(authorizationManager -> authorizationManager.getClass().getName().startsWith("ir.daneshrefah"))
                .map(authorizationManagers -> authorizationManagers.getClass().getSimpleName()).toList();

    }

    public List<String> getAuthorizationConfigList() {
        return authorizationManagerDecisionChainsNames;
    }

    public List<String> getAuthorityList() {
        return authorizationManagerName;
    }
}
