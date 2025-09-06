package ir.daneshrefah.scm.core.authority.decision.constant;

import ir.daneshrefah.scm.core.authority.decision.manager.SecurityContext;
import lombok.Data;
import org.springframework.security.authorization.AuthorizationManager;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuthorizationManagerChainDefinition {
    private final List<Class<? extends AuthorizationManager<SecurityContext>>> authorities = new ArrayList<>();
    private String managerBeanName;
}
