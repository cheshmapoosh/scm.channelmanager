package ir.daneshrefah.scm.core.authority.decision.voter;


import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;

import java.util.ArrayList;
import java.util.List;

public abstract class DecisionVoter {
    List<DecisionVoter>  DECISION_VOTER_LIST = new ArrayList<>();
    /**
     * if any voter return a ACCESS_GRANTED value, the manager accept it and end the checking another voter
     */
    public static final int ACCESS_GRANTED = 1;
    /**if the voter return ACCESS_ABSTAIN it means the voter accepted but manager must check another voters */
    public static final int ACCESS_ABSTAIN = 0;
    /**
     * if the voter return ACCESS_DENIED or throws any AuthorityBaseException it means that rule does not passed and
     * the manager stopped the checking another voters.
     */
    public static final int ACCESS_DENIED = -1;

    public final int vote(Message message, TerminalServiceChannelAccess authObject) {
        boolean isSupport = support(authObject);
        if (!isSupport) {
            return ACCESS_ABSTAIN;
        }
        return vote(message);
    }

    protected abstract int vote(Message message);

    protected abstract boolean support(TerminalServiceChannelAccess service);

}
