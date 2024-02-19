package ir.daneshrefah.scm.core.authority.decision.helper;

import ir.daneshrefah.scm.common.model.message.Authentication;
import ir.daneshrefah.scm.common.model.customer.PersonProfile;
import ir.daneshrefah.scm.common.model.customer.ServiceAccess;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.common.type.ConditionType;
import ir.daneshrefah.scm.core.integration.provider.CustomerServiceImpl;
import ir.daneshrefah.scm.core.model.condition.*;
import ir.daneshrefah.scm.core.service.ConditionService;
import ir.daneshrefah.scm.core.service.ServiceAccessService;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
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

    private final ConditionService conditionService;
    private final ServiceAccessService serviceAccessService;
    private final CustomerServiceImpl customerService;

    private static final Map<Class<? extends BaseCondition>, Map<String, List<BaseCondition>>> CONDITIONS_CACHE =
            new ConcurrentHashMap<>();

    @PostConstruct
    private void prepare() {
        reloadCache();
    }

    public PersonProfile fillServiceAccessForProfile(PersonProfile profile, String terminalCode) {
        /*
        * Controls if the 'service access' data has already been loaded, does not reload.
        * */
        if (null != profile.getServiceAccesses()) {
            return profile;
        }
        List<ServiceAccess> serviceAccesses = serviceAccessService.findByPersonProfileId(profile.getPersonId().username());
        profile.setServiceAccesses(serviceAccesses);
        return profile;
    }

    public List<Condition> findUserConditions(ConditionType conditionType, String terminalCode,
                                              Authentication authentication) {
        return Collections.emptyList();
    }

    public List<Condition> findTerminalConditions(ConditionType conditionType, TerminalServiceAccess serviceAccess,
                                                  Authentication authentication) {
        //checking terminal and service auth and second auth.
        AuthenticationMethod loginAuth = realizeAuthenticationMethod(serviceAccess, authentication, true);
        AuthenticationMethod transactionAuth = realizeAuthenticationMethod(serviceAccess, authentication, false);
        return filterByConditionType(findCompatibleConditions(serviceAccess, loginAuth, transactionAuth), conditionType);
    }

    public void reloadCache() {
        initializeCacheSpace();
        cacheConditions(TerminalCondition.class, conditionService.fetchAllTerminalConditions());
        log.info(">>> terminal conditions successfully cached.");
        cacheConditions(ServiceCondition.class, conditionService.fetchAllServiceConditions());
        log.info(">>> service conditions successfully cached.");
        cacheConditions(TerminalServiceCondition.class, conditionService.fetchAllTerminalServiceConditions());
        log.info(">>> terminal-service conditions successfully cached.");
    }

    private void initializeCacheSpace() {
        createCacheIfNotExists(TerminalCondition.class);
        createCacheIfNotExists(ServiceCondition.class);
        createCacheIfNotExists(TerminalServiceCondition.class);
    }

    private void cacheConditions(Class<? extends BaseCondition> conditionClassType, List<?> conditions) {
        if (conditionClassType.equals(ServiceCondition.class)) {
            conditions.stream().map(ServiceCondition.class::cast).forEach(condition -> putToCache(conditionClassType, condition.getService().getId(), condition));
        } else if (conditionClassType.equals(TerminalCondition.class)) {
            conditions.stream().map(TerminalCondition.class::cast).forEach(condition -> putToCache(conditionClassType, condition.getTerminal().getId(), condition));
        } else if (conditionClassType.equals(TerminalServiceCondition.class)) {
            conditions.stream().map(TerminalServiceCondition.class::cast).forEach(condition -> putToCache(conditionClassType, condition.getTerminalServiceAccess().getId(), condition));
        }
    }

    private void createCacheIfNotExists(Class<? extends BaseCondition> conditionClassType) {
        if (!CONDITIONS_CACHE.containsKey(conditionClassType)) {
            CONDITIONS_CACHE.put(conditionClassType, new ConcurrentHashMap<>());
        }
    }

    /**
     * @param conditionClassType involves {@link ServiceCondition},{@link TerminalCondition} or {@link TerminalServiceCondition}
     * @param condition          condition list like TerminalService condition list
     * @param id                 based on condition type,ex for ServiceConditions using Service ID
     * @implNote This method at first get or create condition ConcurrentHashMap by cacheType, after that
     * check condition list existence by requested id, if the list was created before, get that and add the
     * input condition or else create the list,add the condition and put the list to cache map.
     */
    private void putToCache(Class<? extends BaseCondition> conditionClassType, String id, BaseCondition condition) {
        Map<String, List<BaseCondition>> conditionsCache = CONDITIONS_CACHE.get(conditionClassType);
        List<BaseCondition> valueList;
        if (conditionsCache.containsKey(id)) {
            valueList = conditionsCache.get(id);
            valueList.add(condition);
        } else {
            valueList = new ArrayList<>();
            valueList.add(condition);
            conditionsCache.put(id, valueList);
        }
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

        if (service.getCheckAccessFirstAuthentication() && terminal.isSupportCheckAuthentication()) {
            return authenticationMethod;
        }

        return null;
    }

    private List<Condition> findCompatibleConditions(TerminalServiceAccess serviceAccess, AuthenticationMethod loginAuth, AuthenticationMethod transactionAuth) {
        /* checking Terminal-service-auth-secondAuth conditions */
        List<Condition> conditions = getInnerRoutingCondition(loginAuth, transactionAuth, TerminalServiceCondition.class, serviceAccess.getId());
        if (!conditions.isEmpty()) {
            return conditions;
        }
        /* checking terminal & service */
        //getting auth status from message header.
        Service service = serviceAccess.getService();
        Terminal terminal = serviceAccess.getTerminal();
        List<Condition> serviceConditions = getInnerRoutingCondition(loginAuth, transactionAuth, ServiceCondition.class, service.getId());
        List<Condition> terminalConditions = getInnerRoutingCondition(loginAuth, transactionAuth, TerminalCondition.class, terminal.getId());
        return mergeConditions(serviceConditions, terminalConditions);
    }

    private List<Condition> mergeConditions(List<Condition> terminalServiceAuthentication, List<Condition> terminalServiceSecondAuthentication) {
        List<Condition> result = new ArrayList<>();
        result.addAll(terminalServiceAuthentication);
        result.addAll(terminalServiceSecondAuthentication);
        return result;
    }

    private List<Condition> getInnerRoutingCondition(AuthenticationMethod loginAuth, AuthenticationMethod transactionAuth, Class<? extends BaseCondition> conditionClassType, String id) {
        //check [component]-auth-secAuth
        if (Objects.nonNull(loginAuth) && Objects.nonNull(transactionAuth)) {
            List<Condition> authAndSecAuthConditions = findCustomConditions(conditionClassType, id, loginAuth, transactionAuth);
            if (!authAndSecAuthConditions.isEmpty()) {
                return authAndSecAuthConditions;
            }
        }
        //check [component]-auth
        List<Condition> authConditions = new ArrayList<>();
        if (Objects.nonNull(loginAuth)) {
            authConditions = findCustomConditions(conditionClassType, id, loginAuth, null);
        }
        //check [component]-second-auth
        List<Condition> secAuthConditions = new ArrayList<>();
        if (Objects.nonNull(transactionAuth)) {
            secAuthConditions = findCustomConditions(conditionClassType, id, null, transactionAuth);
        }
        //merge [component]-auth & [component]-sec-auth
        List<Condition> mergeFirstAndSecServiceConditions = mergeConditions(authConditions, secAuthConditions);
        if (!mergeFirstAndSecServiceConditions.isEmpty()) {
            return mergeFirstAndSecServiceConditions;
        }
        //get all service conditions
        return findCustomConditions(conditionClassType, id, null, null);
    }

    private List<Condition> filterByConditionType(List<Condition> conditions, ConditionType conditionType) {
        return conditions
                .stream()
                .filter(condition -> conditionType.equals(condition.getType()))
                .collect(Collectors.toList());
    }

    private List<Condition> findCustomConditions(Class<? extends BaseCondition> conditionClassType, String id, AuthenticationMethod loginAuth, AuthenticationMethod transactionAuth) {
        return CONDITIONS_CACHE
                .get(conditionClassType)
                .getOrDefault(id, new ArrayList<>())
                .stream()
                .filter(condition -> Objects.equals(condition.getLoginAuthenticationMethod(), loginAuth) && Objects.equals(condition.getTransactionAuthenticationMethod(), transactionAuth))
                .map(BaseCondition::getCondition)
                .collect(Collectors.toList());
    }

}
