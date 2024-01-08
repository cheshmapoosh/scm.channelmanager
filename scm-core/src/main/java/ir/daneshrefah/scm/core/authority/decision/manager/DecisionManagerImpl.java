package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.voter.*;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class DecisionManagerImpl implements DecisionManager {

    private final List<DecisionVoter> DECISION_VOTER_LIST;

    public DecisionManagerImpl(ConditionsDecisionVoterImpl conditionsDecisionVoter) {
        List<DecisionVoter> voterList = new ArrayList<>();
        voterList.add(new AuthenticationDecisionVoter());
        voterList.add(new TransactionAuthenticationDecisionVoter());
        voterList.add(new ServiceAssignmentDecisionVoter());
        voterList.add(new AssetAssignmentDecisionVoter());
        voterList.add(conditionsDecisionVoter);
        DECISION_VOTER_LIST = Collections.unmodifiableList(voterList);
    }

    @Override
    public boolean decide(Message message) throws AuthorityBaseException {
        TerminalServiceChannelAccess service = message.getHeader().getService();
        for (DecisionVoter voter : DECISION_VOTER_LIST) {
            int vote = voter.vote(message, service);
            if (vote == DecisionVoter.ACCESS_GRANTED) {
                return true;
            } else if (vote == DecisionVoter.ACCESS_DENIED) {
                return false;
            }
        }
        return true;
    }

}
