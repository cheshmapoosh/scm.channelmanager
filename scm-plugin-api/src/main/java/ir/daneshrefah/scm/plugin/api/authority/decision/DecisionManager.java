package ir.daneshrefah.scm.plugin.api.authority.decision;


import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
@Deprecated
public interface DecisionManager {

    boolean decide(Message message) throws AuthorityBaseException;

}
