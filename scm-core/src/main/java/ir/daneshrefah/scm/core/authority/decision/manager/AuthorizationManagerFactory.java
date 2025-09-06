package ir.daneshrefah.scm.core.authority.decision.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class AuthorizationManagerFactory {

    private final Map<String, AuthorizationManager<AuthorizationData> > authorizationManagers;

    @PostConstruct
    public void init() {
        authorizationManagers
                .values()
                .forEach(manger -> log.info(">>> AuthorizationManager '{}' has been registered", manger.getClass().getName()));
    }

    public List<AuthorizationManager<AuthorizationData>> getAuthorizationManagerList() {
        return authorizationManagers.values().stream().toList();
    }


    public Optional<AuthorizationManager<AuthorizationData>> getAuthorizationManager(Class<? extends AuthorizationManager<?>> type) {
        return authorizationManagers
                .values()
                .stream()
                .filter(manager -> manager.getClass().isAssignableFrom(type))
                .findFirst();
    }

    public Optional<AuthorizationManager<AuthorizationData>> getAuthorizationManager(String beanName) {
        return authorizationManagers.get(beanName) != null ? Optional.of(authorizationManagers.get(beanName)) : Optional.empty();
    }

}
