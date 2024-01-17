package ir.daneshrefah.scm.plugin.api.authority.decision;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-06
 */
public class PermitAllDecisionManager implements DecisionManager {

    @Override
    public boolean decide(Message message) throws AuthorityBaseException {
        return true;
    }

}
