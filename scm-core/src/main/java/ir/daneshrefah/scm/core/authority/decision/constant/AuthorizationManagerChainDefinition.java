package ir.daneshrefah.scm.core.authority.decision.constant;

import lombok.Data;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorizationManager;

import java.util.ArrayList;
import java.util.List;

@Data
public class AuthorizationManagerChainDefinition {
    private final List<Class<? extends AuthorizationManager<Exchange>>> authorities = new ArrayList<>();
    private String managerBeanName;
}
