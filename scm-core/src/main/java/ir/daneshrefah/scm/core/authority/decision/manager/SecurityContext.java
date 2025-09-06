package ir.daneshrefah.scm.core.authority.decision.manager;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.security.authorization.AuthorizationManager;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityContext implements Serializable {
    private Exchange exchange;
    private List<AuthorizationManager<SecurityContext>> authorities;

}
