package ir.daneshrefah.scm.core.authority.decision.manager;

import ir.daneshrefah.scm.common.exception.AccessDeniedException;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import ir.daneshrefah.scm.core.authority.decision.voter.DecisionVoter;
import ir.daneshrefah.scm.core.authority.decision.constant.Priority;
import ir.daneshrefah.scm.plugin.api.authority.decision.DecisionManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DecisionManagerImpl implements DecisionManager {

    @PostConstruct
    private void setupDecisionVoters() {
        /* sorting by decision priority */
        List<DecisionVoter> sortedVoterList = new ArrayList<>();
        sortedVoterList.addAll(filterVoterList(Priority.MAX));
        sortedVoterList.addAll(filterVoterList(Priority.MEDIUM));
        sortedVoterList.addAll(filterVoterList(Priority.LOW));
        DecisionVoter.DECISION_VOTER_LIST.clear();
        DecisionVoter.DECISION_VOTER_LIST.addAll(sortedVoterList);
    }

    @Override
    public void decide(Message message) throws AuthorityBaseException {
        TerminalServiceChannelAccess service = message.getHeader().getService();
        for (DecisionVoter voter : DecisionVoter.DECISION_VOTER_LIST) {
            int vote = voter.vote(message, service);
            if (vote == DecisionVoter.ACCESS_GRANTED) {
                return;
            } else if (vote == DecisionVoter.ACCESS_DENIED) {
                throw new AccessDeniedException();
            }
        }
    }

    private List<DecisionVoter> filterVoterList(Priority priority) {
        return DecisionVoter
                .DECISION_VOTER_LIST
                .stream()
                .filter(decisionVoter -> priority.equals(decisionVoter.priority()))
                .collect(Collectors.toList());
    }

}
