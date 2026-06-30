package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.uaa.exception.activation.nib.NibActivationChannelMappingNotFoundException;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.MembershipChannelAccessRow;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.MembershipChannelServiceAccessRow;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.NibActivationJdbcRepository;
import ir.daneshrefah.scm.uaa.repository.authentication.nib.NibChannelMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@ConditionalOnProperty(
        prefix = "scm.uaa.activation.nib",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequiredArgsConstructor
public class NibMembershipAccessDuplicator {
    private final NibActivationJdbcRepository repository;

    public void duplicate(Integer personId, TerminalType fromTerminal) {
        Map<Integer, Integer> targetChannels = channelMappings(repository.findNibChannelMappings());
        if (targetChannels.isEmpty()) {
            throw new NibActivationChannelMappingNotFoundException("NIB authentication-method channels");
        }

        List<MembershipChannelAccessRow> sourceRows = repository
                .findSourceMembershipChannelAccess(personId, fromTerminal);
        Set<Integer> copiedMemberships = new HashSet<>();
        for (MembershipChannelAccessRow source : sourceRows) {
            // One source row per membership is copied; the query ordering makes this deterministic and idempotent.
            if (!copiedMemberships.add(source.membershipId())) {
                continue;
            }
            Integer targetChannelId = targetChannels.get(source.authenticationMethodId());
            if (targetChannelId == null) {
                throw new NibActivationChannelMappingNotFoundException(
                        "authentication method " + source.authenticationMethodId()
                );
            }
            Integer targetMembershipAccessId = repository.insertMembershipChannelAccess(source, targetChannelId);
            duplicateServices(source, targetMembershipAccessId, targetChannelId);
        }
    }

    private void duplicateServices(
            MembershipChannelAccessRow source,
            Integer targetMembershipAccessId,
            Integer targetChannelId
    ) {
        for (MembershipChannelServiceAccessRow service
                : repository.findMembershipChannelServiceAccess(source.id())) {
            BigDecimal targetServiceAccessId = repository
                    .findTargetChannelServiceAccessId(service.channelServiceAccessId(), targetChannelId)
                    .orElseThrow(() -> new NibActivationChannelMappingNotFoundException("channel service access"));
            repository.insertMembershipChannelServiceAccess(
                    service,
                    targetMembershipAccessId,
                    targetServiceAccessId
            );
        }
    }

    private Map<Integer, Integer> channelMappings(List<NibChannelMapping> mappings) {
        Map<Integer, Integer> result = new HashMap<>();
        for (NibChannelMapping mapping : mappings) {
            Integer previous = result.put(mapping.authenticationMethodId(), mapping.channelId());
            if (previous != null && !previous.equals(mapping.channelId())) {
                throw new NibActivationChannelMappingNotFoundException(
                        "ambiguous authentication method " + mapping.authenticationMethodId()
                );
            }
        }
        return result;
    }
}
