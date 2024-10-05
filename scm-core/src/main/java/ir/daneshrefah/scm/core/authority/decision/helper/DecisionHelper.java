package ir.daneshrefah.scm.core.authority.decision.helper;

import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.common.model.customer.ServiceAccess;
import ir.daneshrefah.scm.common.model.customer.UserProfile;
import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.core.model.condition.BaseCondition;
import ir.daneshrefah.scm.core.model.condition.ServiceCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalServiceCondition;
import ir.daneshrefah.scm.core.service.ConditionService;
import ir.daneshrefah.scm.core.service.ServiceAccessService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.MessageInputContext;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DecisionHelper {

    private static final Map<String, List<BaseCondition>> CONDITIONS_CACHE = new ConcurrentHashMap<>();
    private final ConditionService conditionService;
    private final ServiceAccessService serviceAccessService;
    private final TerminalService terminalService;



    @PostConstruct
    private void prepare() {
        reloadCache();
    }

    public UserProfile fillServiceAccessForProfile(


            UserProfile profile, String terminalCode) {
        /*
         * Controls if the 'service access' data has already been loaded, does not reload.
         * */
        if (null != profile.getServiceAccesses()) {
            return profile;
        }
        List<ServiceAccess> serviceAccesses = serviceAccessService.findByPersonUsername(profile.getPersonUsername());
        profile.setServiceAccesses(serviceAccesses);
        return profile;
    }

    public List<Condition> findUserConditions(ConditionType conditionType, String terminalCode,
                                              Authentication authentication) {
        return Collections.emptyList(); //TODO
    }

    public List<Condition> findTerminalConditions(ConditionType conditionType, Service service,
                                                  Authentication authentication) {
        TerminalServiceAccess serviceAccess = terminalService.findTerminalServiceAccessByTerminalCodeAndServiceCode(
                MessageInputContext.getCurrentContext().getTerminalCode(), service.getCode()).orElse(null);
        //checking terminal and service auth and second auth.
        AuthenticationMethod loginAuth = realizeAuthenticationMethod(serviceAccess, authentication, true);
        AuthenticationMethod transactionAuth = realizeAuthenticationMethod(serviceAccess, authentication, false);
        return filterByConditionType(findCompatibleConditions(serviceAccess, loginAuth, transactionAuth), conditionType);
    }

    public void reloadCache() {
        cacheConditions(conditionService.fetchAllTerminalConditions());
        log.info(">>> terminal conditions successfully cached.");
        cacheConditions(conditionService.fetchAllServiceConditions());
        log.info(">>> service conditions successfully cached.");
        cacheConditions(conditionService.fetchAllTerminalServiceConditions());
        log.info(">>> terminal-service conditions successfully cached.");
    }


    private void cacheConditions(List<? extends BaseCondition> conditions) {
        //cache condition list by component id (service id,terminal id or service-terminal id)
        conditions
                .forEach(baseCondition -> {
                    String cacheId;
                    if (baseCondition instanceof ServiceCondition) {
                        //casting for get service id
                        cacheId = ((ServiceCondition) baseCondition).getService().getId();
                    } else if (baseCondition instanceof TerminalCondition) {
                        //casting for get terminal id
                        cacheId = ((TerminalCondition) baseCondition).getTerminal().getId();
                    } else {
                        //casting for get terminal-service id
                        cacheId = String.valueOf(((TerminalServiceCondition) baseCondition).getTerminalServiceAccess().getId());
                    }
                    List<BaseCondition> foundConditionList = CONDITIONS_CACHE.getOrDefault(cacheId, new ArrayList<>());
                    foundConditionList.add(baseCondition);
                    CONDITIONS_CACHE.put(cacheId, foundConditionList);
                });

    }


    private AuthenticationMethod realizeAuthenticationMethod(TerminalServiceAccess authObject,
                                                             Authentication authentication, boolean isLogin) {
        AuthenticationMethod authenticationMethod = null;
        if (null != authentication && !authentication.isAnonymous() && authentication instanceof UserAuthentication) {
            if (isLogin) {
                authenticationMethod = ((UserAuthentication) authentication).getPrincipal().getLoginAuthenticationMethod();
            } else {
                authenticationMethod = ((UserAuthentication) authentication).getPrincipal().getTransactionAuthenticationMethod();
            }
        }

        Service service = authObject.getService();
        Terminal terminal = authObject.getTerminal();

        if (isLogin && service.getCheckAccessFirstAuthentication() && terminal.isSupportCheckAuthentication()) {
            return authenticationMethod;
        } else if (!isLogin && service.getCheckAccessSecondAuthentication() && terminal.isSupportCheckSecondAuthentication()) {
            return authenticationMethod;
        }

        return null;
    }

    private List<Condition> findCompatibleConditions(TerminalServiceAccess serviceAccess, AuthenticationMethod loginAuth, AuthenticationMethod transactionAuth) {
        /* checking Terminal-service-auth-secondAuth conditions */
        List<Condition> conditions = getInnerRoutingCondition(loginAuth, transactionAuth, String.valueOf(serviceAccess.getId()));
        if (!conditions.isEmpty()) {
            return conditions;
        }
        /* checking terminal & service */
        //getting auth status from message header.
        Service service = serviceAccess.getService();
        Terminal terminal = serviceAccess.getTerminal();
        List<Condition> serviceConditions = getInnerRoutingCondition(loginAuth, transactionAuth, service.getId());
        List<Condition> terminalConditions = getInnerRoutingCondition(loginAuth, transactionAuth, terminal.getId());
        return mergeConditions(serviceConditions, terminalConditions);
    }

    private List<Condition> mergeConditions(List<Condition> terminalServiceAuthentication, List<Condition> terminalServiceSecondAuthentication) {
        List<Condition> result = new ArrayList<>();
        result.addAll(terminalServiceAuthentication);
        result.addAll(terminalServiceSecondAuthentication);
        return result;
    }

    private List<Condition> getInnerRoutingCondition(AuthenticationMethod loginAuth, AuthenticationMethod transactionAuth, String id) {
        //check [component]-auth-secAuth
        if (Objects.nonNull(loginAuth) && Objects.nonNull(transactionAuth)) {
            List<Condition> authAndSecAuthConditions = findCustomConditions(id, loginAuth, transactionAuth);
            if (!authAndSecAuthConditions.isEmpty()) {
                return authAndSecAuthConditions;
            }
        }
        //check [component]-auth
        List<Condition> authConditions = new ArrayList<>();
        if (Objects.nonNull(loginAuth)) {
            authConditions = findCustomConditions(id, loginAuth, null);
        }
        //check [component]-second-auth
        List<Condition> secAuthConditions = new ArrayList<>();
        if (Objects.nonNull(transactionAuth)) {
            secAuthConditions = findCustomConditions(id, null, transactionAuth);
        }
        //merge [component]-auth & [component]-sec-auth
        List<Condition> mergeFirstAndSecServiceConditions = mergeConditions(authConditions, secAuthConditions);
        if (!mergeFirstAndSecServiceConditions.isEmpty()) {
            return mergeFirstAndSecServiceConditions;
        }
        //get all service conditions
        return findCustomConditions(id, null, null);
    }

    private List<Condition> filterByConditionType(List<Condition> conditions, ConditionType conditionType) {
        return conditions
                .stream()
                .filter(condition -> conditionType.equals(condition.getType()))
                .collect(Collectors.toList());
    }

    private List<Condition> findCustomConditions(String id, AuthenticationMethod loginAuth, AuthenticationMethod transactionAuth) {
        return CONDITIONS_CACHE
                .getOrDefault(id, Collections.emptyList())
                .stream()
                .filter(condition -> Objects.equals(condition.getLoginAuthenticationMethod(), loginAuth) && Objects.equals(condition.getTransactionAuthenticationMethod(), transactionAuth))
                .map(BaseCondition::getCondition)
                .collect(Collectors.toList());
    }

}
