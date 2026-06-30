package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationChannelMappingNotFoundException;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.NibActivationJdbcRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.NibRoleRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class NibRoleProvisioningService {
    static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    static final String ROLE_CORPORATE_CUSTOMER = "ROLE_CORPORATE_CUSTOMER";

    private final NibActivationJdbcRepository repository;

    public void provision(Integer personId, PersonType personType) {
        Map<String, NibRoleRow> roles = repository
                .findRoles(Set.of(ROLE_CUSTOMER, ROLE_CORPORATE_CUSTOMER))
                .stream()
                .collect(Collectors.toMap(NibRoleRow::code, Function.identity()));

        ensureRole(personId, requiredRole(roles, ROLE_CUSTOMER));
        if (personType == PersonType.CORPORATE) {
            ensureRole(personId, requiredRole(roles, ROLE_CORPORATE_CUSTOMER));
        }
    }

    private NibRoleRow requiredRole(Map<String, NibRoleRow> roles, String code) {
        NibRoleRow role = roles.get(code);
        if (role == null) {
            throw new NibActivationChannelMappingNotFoundException("role " + code);
        }
        return role;
    }

    private void ensureRole(Integer personId, NibRoleRow role) {
        // Role provisioning is intentionally idempotent: an existing assignment is already the target state.
        if (!repository.userHasRole(personId, role.id())) {
            repository.insertUserRole(personId, role.id());
        }
    }
}
