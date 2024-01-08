package ir.daneshrefah.scm.core.authority.decision.voter;

import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.model.condition.Condition;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceChannelAccess;
import ir.daneshrefah.scm.core.authority.decision.cache.CacheConditionService;
import ir.daneshrefah.scm.core.authority.decision.constant.ConditionCacheType;
import ir.daneshrefah.scm.plugin.api.authority.exception.AuthorityBaseException;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@Qualifier
@RequiredArgsConstructor
@Slf4j
public class ConditionsDecisionVoterImpl extends DecisionVoter {

    private final CacheConditionService cacheConditionService;

    @Override
    public int vote(Message message) throws AuthorityBaseException {
//        check withdraw
//        check authority
        TerminalServiceChannelAccess authObject = message.getHeader().getService();

        //checking terminal and service auth and second auth.
        AuthenticationMethod loginAuthenticationMethod = realizeLoginAuthenticationMethod(authObject, message);
        AuthenticationMethod transactionAuthenticationMethod = realizeTransactionAuthenticationMethod(authObject, message);
        //checking Terminal-service-auth-secondAuth conditions
        int terminalServiceVote = checkTerminalServiceAccess(loginAuthenticationMethod, transactionAuthenticationMethod,authObject);
        if (terminalServiceVote >= ACCESS_ABSTAIN) {
            return terminalServiceVote;
        }
        //checking terminal & service
        int serviceAndTerminalVote = checkServiceAccessAndTerminalAccess(loginAuthenticationMethod,transactionAuthenticationMethod, authObject);
        if (serviceAndTerminalVote >= ACCESS_ABSTAIN) {
            return serviceAndTerminalVote;
        }
        return ACCESS_DENIED;
    }

    @Override
    protected boolean support(TerminalServiceChannelAccess service) {
        return true;
    }

    private AuthenticationMethod realizeLoginAuthenticationMethod(TerminalServiceChannelAccess authObject, Message message) {
        UserAuthentication authentication = (UserAuthentication) message.getHeader().getAuthentication();
        TerminalServiceAccess service = message.getHeader().getService().getTerminalServiceAccess();
        boolean isAuthenticationSupport = authentication.isAuthenticated() && !authentication.isAnonymous() &&
                service.getService().getCheckAccessFirstAuthentication() && service.getTerminal().getSupportCheckAuthentication();
        if (!isAuthenticationSupport) {
            return null;
        }
        return authentication.getPrincipal().getLoginAuthenticationMethod();
    }

    private AuthenticationMethod realizeTransactionAuthenticationMethod(TerminalServiceChannelAccess authObject, Message message) throws NullPointerException {
        UserAuthentication authentication = (UserAuthentication) message.getHeader().getAuthentication();
        TerminalServiceAccess service = message.getHeader().getService().getTerminalServiceAccess();
        boolean isAuthenticationSupport = authentication.isAuthenticated() && !authentication.isAnonymous() &&
                service.getService().getCheckAccessSecondAuthentication() && service.getTerminal().getSupportCheckSecondAuthentication();
        if (!isAuthenticationSupport) {
            return null;
        }
        return authentication.getPrincipal().getTransactionAuthenticationMethod();
    }

    private int checkServiceAccessAndTerminalAccess(AuthenticationMethod firstAuth,AuthenticationMethod secondAuth, TerminalServiceChannelAccess authObject) {
        //getting auth status from message header.
        Service service = authObject.getTerminalServiceAccess().getService();
        Terminal terminal = authObject.getTerminalServiceAccess().getTerminal();
        List<Condition> serviceConditions = getInnerRoutingCondition(firstAuth,secondAuth,ConditionCacheType.SERVICE,service.getId());
        List<Condition> terminalConditions = getInnerRoutingCondition(firstAuth,secondAuth,ConditionCacheType.TERMINAL,terminal.getId());
        List<Condition> mergedConditions = mergeConditions(serviceConditions, terminalConditions);
        //TODO CHECK CONDITION AND RETURN VOTE
        return 0;
    }

    private int checkTerminalServiceAccess(AuthenticationMethod firstAuth,AuthenticationMethod secondAuth,TerminalServiceChannelAccess authObject) {
        List<Condition> conditions = getInnerRoutingCondition(firstAuth, secondAuth, ConditionCacheType.TERMINAL_SERVICE, authObject.getId());
        //TODO CHECK CONDITION AND RETURN VOTE
        return 0;
    }

    private List<Condition> getInnerRoutingCondition(AuthenticationMethod firstAuth,AuthenticationMethod secondAuth,ConditionCacheType conditionType,String id){

        //check [component]-auth-secAuth
        if (Objects.nonNull(firstAuth) && Objects.nonNull(secondAuth)){
            List<Condition> authAndSecAuthConditions = cacheConditionService.findCustomConditions(conditionType, id, firstAuth, secondAuth);
            if (!authAndSecAuthConditions.isEmpty()){
                return authAndSecAuthConditions;
            }
        }
        //check [component]-auth
        List<Condition> authConditions = new ArrayList<>();
        if (Objects.nonNull(firstAuth)){
            authConditions = cacheConditionService.findCustomConditions(conditionType,id,firstAuth,null);
        }
        //check [component]-second-auth
        List<Condition> secAuthConditions = new ArrayList<>();
        if (Objects.nonNull(secondAuth)){
            secAuthConditions = cacheConditionService.findCustomConditions(conditionType,id,null,secondAuth);
        }
        //merge [component]-auth & [component]-sec-auth
        List<Condition> mergeFirstAndSecServiceConditions = mergeConditions(authConditions, secAuthConditions);
        if (!mergeFirstAndSecServiceConditions.isEmpty()){
            return mergeFirstAndSecServiceConditions;
        }
        //get all service conditions
        return cacheConditionService.findCustomConditions(conditionType,id,null,null);
    }

    private List<Condition> mergeConditions(List<Condition> terminalServiceAuthentication, List<Condition> terminalServiceSecondAuthentication) {
        List<Condition> result = new ArrayList<>();
        result.addAll(terminalServiceAuthentication);
        result.addAll(terminalServiceSecondAuthentication);
        return result;
    }

}
