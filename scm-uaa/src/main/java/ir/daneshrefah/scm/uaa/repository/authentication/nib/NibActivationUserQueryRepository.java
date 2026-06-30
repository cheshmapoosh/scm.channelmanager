package ir.daneshrefah.scm.uaa.repository.authentication.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.repository.authentication.UserRepository;
import ir.daneshrefah.scm.uaa.service.activation.nib.NibActivationUserLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class NibActivationUserQueryRepository implements NibActivationUserLookup {
    private final UserRepository userRepository;

    @Override
    @Transactional(transactionManager = "mainTransactionManager", readOnly = true)
    public boolean existsUser(String username, TerminalType terminal) {
        return username != null
                && terminal != null
                && userRepository.existsByNicknameAndTerminalId(
                username,
                terminal.getLegacyTerminalId().intValue()
        );
    }
}
