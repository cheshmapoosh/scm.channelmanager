package ir.daneshrefah.scm.core.authority.decision.configuration.model;

import lombok.*;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorizationManager;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityContext implements Serializable {
    private Exchange exchange;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ManagerSecurityContext extends SecurityContext implements Serializable {
        private List<AuthorizationManager<SecurityContext>> authorities;
    }

}
