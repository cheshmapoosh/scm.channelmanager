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
    int ACCESS_GRANTED = 1;
    int ACCESS_ABSTAIN = 0;
    int ACCESS_DENIED = -1;

    int vote(Message message, TerminalServiceChannelAccess authObject) throws AuthorityBaseException;

    Priority priority();

    @PostConstruct
    default void init(){
        DECISION_VOTER_LIST.add(this);
    }
}
