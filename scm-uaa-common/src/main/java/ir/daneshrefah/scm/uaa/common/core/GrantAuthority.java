package ir.daneshrefah.scm.uaa.common.core;

import ir.daneshrefah.scm.uaa.common.constants.RoleAuthority;
import ir.daneshrefah.scm.uaa.common.constants.ScopeAuthority;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * @apiNote : @PreAuthorize("hasAuthority(@grant.scopes.ACTIVATION)")
 */
@Component("grant")
@Getter
public class GrantAuthority {

    private final RoleAuthority roles = new RoleAuthority() {
    };
    private final ScopeAuthority scopes = new ScopeAuthority() {
    };

}
