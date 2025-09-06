package ir.daneshrefah.scm.core.authority.decision.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.Introspector;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuthorizationManagerFactory {

    private static final Map<String, AuthorizationManager<SecurityContext>> AUTHORIZATION_CHECK_MANAGERS = new ConcurrentHashMap<>();
    private static final Map<String, AuthorizationManager<SecurityContext>> AUTHORIZATION_VOTERS = new ConcurrentHashMap<>();
    private final Map<String, AuthorizationManager<SecurityContext>> authorizationManagers;

    @PostConstruct
    public void init() {
        authorizationManagers.forEach((key, value) -> {
            if (value instanceof AuthorityManager) {
                AUTHORIZATION_CHECK_MANAGERS.put(key, value);
                log.info(">>> Authorization Manager '{}' has been registered", value.getClass().getName());
            } else {
                AUTHORIZATION_VOTERS.put(key, value);
                log.info(">>> Authorization Voter '{}' has been registered", value.getClass().getName());
            }
        });
    }

    public List<AuthorizationManager<SecurityContext>> getAuthorizationCheckManagers() {
        return AUTHORIZATION_CHECK_MANAGERS.values().stream().toList();
    }

    public List<AuthorizationManager<SecurityContext>> getAuthorizationVoters() {
        return AUTHORIZATION_VOTERS.values().stream().toList();
    }


    public Optional<AuthorizationManager<SecurityContext>> getAuthorizationManager(String beanName) {
        return Optional.ofNullable(AUTHORIZATION_CHECK_MANAGERS.get(beanName));
    }

    public Optional<AuthorizationManager<SecurityContext>> getAuthorizationManager(Class<?> beanClass) {
        return Optional.ofNullable(AUTHORIZATION_CHECK_MANAGERS.get(Introspector.decapitalize(beanClass.getSimpleName())));
    }

    public Optional<AuthorizationManager<SecurityContext>> getAuthorizationVoter(String beanName) {
        return Optional.ofNullable(AUTHORIZATION_VOTERS.get(beanName));
    }

    public Optional<AuthorizationManager<SecurityContext>> getAuthorizationVoter(Class<?> beanClass) {
        return Optional.ofNullable(AUTHORIZATION_VOTERS.get(Introspector.decapitalize(beanClass.getSimpleName())));
    }

}
