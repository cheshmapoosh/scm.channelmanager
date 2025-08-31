package ir.daneshrefah.scm.core.authority.decision.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Vote {
    /**
     * if any voter returns a ACCESS_GRANTED value, the manager accepts it and ends the checking another voter
     */
    ACCESS_GRANTED(1),
    /**
     * if the voter returns ACCESS_ABSTAIN, it means the voter accepted but the manager must check another voter
     */
    ACCESS_ABSTAIN(0),
    /**
     * if the voter returns ACCESS_DENIED or throws any AuthorityBaseException, it means that the rule does not pass and
     * the manager stopped the checking another voter.
     */
    ACCESS_DENIED(-1);

    private final int value;
}
