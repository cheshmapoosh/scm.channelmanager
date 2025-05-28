package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.ScmService;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.core.authority.decision.helper.DecisionHelper;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.MessageInputContext;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@RequiredArgsConstructor
public abstract class BaseConditionalDecisionVoter extends DecisionVoter {

    protected final DecisionHelper decisionHelper;

    @Override
    protected final int vote(Message message) {
        Authentication authentication = AuthenticationUtils.getScmAuthentication();
        ConditionType conditionType = getConditionType();
        List<Condition> conditions = decisionHelper.findUserConditions(conditionType,
                MessageInputContext.getCurrentContext().getTerminalCode(), authentication);
        List<Condition> terminalConditions = decisionHelper.findTerminalConditions(conditionType,
                message.getHeader().getService(), authentication);
        CollectionUtils.addAll(conditions, terminalConditions.iterator());

        return checkConditions(message, terminalConditions);
    }

    protected abstract int checkConditions(Message message, List<Condition> conditions);

    protected abstract ConditionType getConditionType();

    @Override
    protected boolean support(ScmService service) {
        return true;
    }

}
