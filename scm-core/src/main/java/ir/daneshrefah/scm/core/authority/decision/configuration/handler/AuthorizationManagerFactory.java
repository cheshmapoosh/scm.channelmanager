package ir.daneshrefah.scm.core.authority.decision.configuration.handler;

import ir.daneshrefah.scm.core.authority.decision.configuration.model.AuthoritiesSecurityContext;
import ir.daneshrefah.scm.core.authority.decision.configuration.model.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.Introspector;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuthorizationManagerFactory {

    private final Map<String, AuthorizationManager<SecurityContext>> authorities;
    private final Map<String, AuthorizationManager<AuthoritiesSecurityContext>> managerAuthorities;

    @PostConstruct
    public void init() {
        managerAuthorities.forEach((beanName, manager) -> log.info("Authorization Manage initialized ,name: {} , manager: {}", beanName, manager));
        authorities.forEach((beanName, manager) -> log.info("Authority rule initialized, name: {} , manager: {}", beanName, manager));
    }

    public List<AuthorizationManager<AuthoritiesSecurityContext>> getAuthorizationCheckManagers() {
        return managerAuthorities.values().stream().toList();
    }

    public List<AuthorizationManager<SecurityContext>> getAuthorizationVoters() {
        return authorities.values().stream().toList();
    }


    public Optional<AuthorizationManager<AuthoritiesSecurityContext>> getAuthorizationManager(String beanName) {
        return Optional.ofNullable(managerAuthorities.get(beanName));
    }

    public Optional<AuthorizationManager<AuthoritiesSecurityContext>> getAuthorizationManager(Class<?> beanClass) {
        return Optional.ofNullable(managerAuthorities.get(Introspector.decapitalize(beanClass.getSimpleName())));
    }

    public Optional<AuthorizationManager<SecurityContext>> getAuthorizationVoter(String beanName) {
        return Optional.ofNullable(authorities.get(beanName));
    }

    public Optional<AuthorizationManager<SecurityContext>> getAuthorizationVoter(Class<?> beanClass) {
        return Optional.ofNullable(authorities.get(Introspector.decapitalize(beanClass.getSimpleName())));
    }

}
