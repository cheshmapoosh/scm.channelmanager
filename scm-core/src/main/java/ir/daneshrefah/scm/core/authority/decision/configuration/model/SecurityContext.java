package ir.daneshrefah.scm.core.authority.decision.configuration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.camel.Exchange;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityContext implements Serializable {
    private Exchange exchange;
    private List<String> serviceAcceptableRoles;
    private List<String> userRoles;

}
