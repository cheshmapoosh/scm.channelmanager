package ir.daneshrefah.scm.core.authority.decision.cache;

import ir.daneshrefah.scm.core.authority.decision.constant.ConditionCacheType;
import ir.daneshrefah.scm.core.model.condition.*;
import ir.daneshrefah.scm.core.service.ConditionService;
import ir.daneshrefah.scm.uaa.common.type.AuthenticationMethod;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CacheConditionService {

    private final ConditionService conditionService;

    /* cache terminal condition by terminal id */
    private static final Map<String, List<BaseCondition<String>>> TERMINAL_CONDITION_CACHE = new ConcurrentHashMap<>();
    /* cache service condition by service id */
    private static final Map<String, List<BaseCondition<String>>> SERVICE_CONDITION_CACHE = new ConcurrentHashMap<>();
    /* cache terminal-service condition by terminal-service-access id */
    private static final Map<String, List<BaseCondition<String>>> TERMINAL_SERVICE_CONDITION_CACHE = new ConcurrentHashMap<>();

    @PostConstruct
    private void prepare() {
        reloadCache();
    }

    public void reloadCache() {
        log.info(">>> fetching terminal conditions ... ");
        cacheTerminalConditions();
        log.info(">>> terminal conditions successfully cached.");
        log.info(">>> fetching service conditions ... ");
        cacheServiceConditions();
        log.info(">>> service conditions successfully cached.");
        log.info(">>> fetching terminal-service conditions ... ");
        cacheTerminalServiceConditions();
        log.info(">>> terminal-service conditions successfully cached.");
    }

    private void cacheTerminalServiceConditions() {
        conditionService
                .fetchAllTerminalServiceConditions()
                .forEach(condition -> {
                    addToCacheMap(TERMINAL_SERVICE_CONDITION_CACHE,condition.getTerminalServiceAccess().getId(),condition);
                });
    }

    private void cacheServiceConditions() {
        conditionService
                .fetchAllServiceConditions()
                .forEach(condition -> {
                    addToCacheMap(SERVICE_CONDITION_CACHE,condition.getService().getId(), condition);
                });
    }

    private void cacheTerminalConditions() {
        conditionService
                .fetchAllTerminalConditions()
                .forEach(condition -> {
                    addToCacheMap(TERMINAL_CONDITION_CACHE,condition.getTerminal().getId(), condition);
                });
    }

    public  List<Condition> findCustomConditions(ConditionCacheType type, String id, AuthenticationMethod firstAuth, AuthenticationMethod secondAuth) {
        List<BaseCondition<String>> conditions = new ArrayList<>();
        if (ConditionCacheType.TERMINAL_SERVICE.equals(type)) {
            filterAuthAndSecondAuth(TerminalServiceCondition.class,id,firstAuth,secondAuth,TERMINAL_SERVICE_CONDITION_CACHE,conditions);
        } else if (ConditionCacheType.SERVICE.equals(type)) {
            filterAuthAndSecondAuth(ServiceCondition.class,id,firstAuth,secondAuth,SERVICE_CONDITION_CACHE,conditions);
        } else if (ConditionCacheType.TERMINAL.equals(type)) {
            filterAuthAndSecondAuth(TerminalCondition.class,id,firstAuth,secondAuth,TERMINAL_CONDITION_CACHE,conditions);
        }
        return findConditions(conditions);
    }

    public  List<Condition> findAllConditions(ConditionCacheType type, String id) {
        List<BaseCondition<String>> conditions = new ArrayList<>();
        if (ConditionCacheType.TERMINAL_SERVICE.equals(type)) {
            findAll(TerminalServiceCondition.class,id,TERMINAL_SERVICE_CONDITION_CACHE,conditions);
        } else if (ConditionCacheType.SERVICE.equals(type)) {
            findAll(ServiceCondition.class,id,SERVICE_CONDITION_CACHE,conditions);
        } else if (ConditionCacheType.TERMINAL.equals(type)) {
            findAll(TerminalCondition.class,id,TERMINAL_CONDITION_CACHE,conditions);
        }
        return findConditions(conditions);
    }

    private  <T extends BaseCondition<String>> List<Condition> findConditions(List<T> baseConditions){
        return baseConditions
                .stream()
                .map(BaseCondition::getCondition)
                .collect(Collectors.toList());
    }

    private <T extends BaseCondition<String>> void filterAuthAndSecondAuth(Class<T> type, String id, AuthenticationMethod firstAuth, AuthenticationMethod secondAuth, Map<?, List<BaseCondition<String>>> cache, List<BaseCondition<String>> target) {
        if (cache.containsKey(id)) {
            cache.get(id)
                    .stream()
                    .filter(baseCondition ->
                        Objects.equals(baseCondition.getAuthenticationMethod(),firstAuth)
                                && Objects.equals(baseCondition.getSecondAuthenticationMethod(),(secondAuth))
                    )
                    .map(type::cast)
                    .forEach(target::add);
        }
    }

    private <T extends BaseCondition<String>> void findAll(Class<T> type, String id, Map<?, List<BaseCondition<String>>> cache, List<BaseCondition<String>> target) {
        if (cache.containsKey(id)) {
            cache.get(id)
                    .stream()
                    .map(type::cast)
                    .forEach(target::add);
        }
    }

    private <T extends BaseCondition<String>> void addToCacheMap(Map<String, List<T>> cache, String key, T value) {
        List<T> valueList;
        if (cache.containsKey(key)){
            valueList = cache.get(key);
            valueList.add(value);
        }else {
            valueList = new ArrayList<>();
            valueList.add(value);
            cache.put(key,valueList);
        }
    }

}
