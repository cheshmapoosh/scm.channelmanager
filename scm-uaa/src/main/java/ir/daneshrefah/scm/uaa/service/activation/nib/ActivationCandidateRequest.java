package ir.daneshrefah.scm.uaa.service.activation.nib;

import ir.daneshrefah.scm.common.constant.TerminalType;

import java.util.Set;

public record ActivationCandidateRequest(
        String username,
        Set<String> scopes,
        TerminalType fromTerminal,
        TerminalType toTerminal
) {
    public ActivationCandidateRequest {
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
    }
}
