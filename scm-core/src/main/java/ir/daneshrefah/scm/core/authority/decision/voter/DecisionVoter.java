package ir.daneshrefah.scm.core.authority.decision.voter;


import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.constant.Priority;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;

public interface DecisionVoter {
    List<DecisionVoter>  DECISION_VOTER_LIST = new ArrayList<>();
    /**
     * if any voter return a ACCESS_GRANTED value, the manager accept it and end the checking another voter
     */
    int ACCESS_GRANTED = 1;
    /**if the voter return ACCESS_ABSTAIN it means the voter accepted but manager must check another voters */
    int ACCESS_ABSTAIN = 0;
    /**
     * if the voter return ACCESS_DENIED or throws any AuthorityBaseException it means that rule does not passed and
     * the manager stopped the checking another voters.
     */
    int ACCESS_DENIED = -1;

    int vote(Message message, TerminalServiceChannelAccess authObject) throws AuthorityBaseException;
    /**
     * the manager order the voters by this priority.
     */
    Priority priority();

    @PostConstruct
    default void init(){
        DECISION_VOTER_LIST.add(this);
    }
}
