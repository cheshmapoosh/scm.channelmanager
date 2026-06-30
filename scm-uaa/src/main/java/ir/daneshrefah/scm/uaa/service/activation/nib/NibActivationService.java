package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationSourceUserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
@Slf4j
public class NibActivationService {
    private final NibRoleProvisioningService roleProvisioningService;
    private final NibChannelAuthenticationDuplicator channelAuthenticationDuplicator;
    private final NibMembershipAccessDuplicator membershipAccessDuplicator;

    @Transactional(transactionManager = "mainTransactionManager")
    public void activate(GeneralPerson person, TerminalType fromTerminal) {
        if (person == null || person.getId() == null || fromTerminal == null) {
            throw new NibActivationSourceUserNotFoundException();
        }

        roleProvisioningService.provision(person.getId(), person.getPersonType());
        channelAuthenticationDuplicator.duplicate(person.getId(), fromTerminal);
        membershipAccessDuplicator.duplicate(person.getId(), fromTerminal);
        log.info("NIB activation completed successfully");
    }
}
