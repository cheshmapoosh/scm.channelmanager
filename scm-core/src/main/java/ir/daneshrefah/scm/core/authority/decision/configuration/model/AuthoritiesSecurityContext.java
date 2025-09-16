package ir.daneshrefah.scm.core.authority.decision.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.authorization.AuthorizationManager;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthoritiesSecurityContext extends SecurityContext implements Serializable {
    private List<AuthorizationManager<SecurityContext>> authorities;
}