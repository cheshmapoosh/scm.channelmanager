package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationAlreadyExistsException;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationChannelMappingNotFoundException;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationSourceUserNotFoundException;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.NibActivationJdbcRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.UserChannelAuthenticationRow;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class NibChannelAuthenticationDuplicator {
    private final NibActivationJdbcRepository repository;

    public void duplicate(Integer personId, TerminalType fromTerminal) {
        if (repository.nibUserChannelAuthenticationExists(personId)) {
            throw new NibActivationAlreadyExistsException();
        }
        UserChannelAuthenticationRow source = repository
                .findSourceUserChannelAuthentication(personId, fromTerminal)
                .orElseThrow(NibActivationSourceUserNotFoundException::new);
        Integer targetChannelId = repository
                .findParentNibChannelId()
                .orElseThrow(() -> new NibActivationChannelMappingNotFoundException("parent NIB channel"));
        repository.insertUserChannelAuthentication(source, targetChannelId);
    }
}
