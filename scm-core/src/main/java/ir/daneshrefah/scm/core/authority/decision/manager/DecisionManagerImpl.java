package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.service.PersonProfileLoader;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.core.authority.decision.voter.*;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class DecisionManagerImpl implements DecisionManager {

    private final List<DecisionVoter> DECISION_VOTER_LIST;

    public DecisionManagerImpl(PersonProfileLoader personProfileLoader, DecisionHelper decisionHelper) {
        List<DecisionVoter> voterList = new ArrayList<>();
        voterList.add(new AuthenticationDecisionVoter());
        voterList.add(new TransactionAuthenticationDecisionVoter());
        voterList.add(new AssetAssignmentDecisionVoter(personProfileLoader));
        voterList.add(new ServiceAssignmentDecisionVoter(personProfileLoader));
        voterList.add(new RateLimitConditionalDecisionVoter(decisionHelper));
        voterList.add(new AuthorityConditionalDecisionVoter(decisionHelper));
        voterList.add(new WithdrawConditionalDecisionVoter(decisionHelper));
        DECISION_VOTER_LIST = Collections.unmodifiableList(voterList);
    }

    @Override
    public boolean decide(Message message) throws AuthorityBaseException {
        Service service = message.getHeader().getService();
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
