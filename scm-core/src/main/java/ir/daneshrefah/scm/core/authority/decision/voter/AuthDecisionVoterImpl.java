package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.constant.Priority;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier
public class AuthDecisionVoterImpl implements DecisionVoter {

    @Override
    public int vote(Message message, TerminalServiceChannelAccess authObject) {
      return  ACCESS_ABSTAIN;
    }

    @Override
    public Priority priority() {
        return Priority.MAX;
    }
}
