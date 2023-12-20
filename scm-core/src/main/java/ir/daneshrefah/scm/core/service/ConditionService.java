package ir.daneshrefah.scm.core.service;


import ir.daneshrefah.scm.core.model.condition.Condition;
import ir.daneshrefah.scm.core.model.condition.ServiceCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalServiceCondition;
import ir.daneshrefah.scm.core.entity.condition.ConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.ServiceConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalServiceConditionEntity;
import ir.daneshrefah.scm.core.mapper.ConditionMapper;
import ir.daneshrefah.scm.core.repository.ConditionRepository;
import ir.daneshrefah.scm.core.repository.ServiceConditionRepository;
import ir.daneshrefah.scm.core.repository.TerminalConditionRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionService {

    private final ConditionRepository conditionRepository;
    private final ServiceConditionRepository serviceConditionRepository;
    private final TerminalConditionRepository terminalConditionRepository;
    private final TerminalServiceConditionRepository serviceTerminalConditionRepository;

    //-- cached data
    private static final Map<String,TerminalCondition> TERMINAL_CONDITION_MAP = new ConcurrentHashMap<>();
    private static final Map<String,TerminalCondition> SERVICE_CONDITION_MAP = new ConcurrentHashMap<>();
    private static final Map<String,TerminalCondition> TERMINAL_SERVICE_CONDITION_MAP = new ConcurrentHashMap<>();



    public List<Condition> fetchAllCondition(){
        List<ConditionEntity> allConditions = conditionRepository.findAll();
        log.info(">>> all {} [conditions] successfully fetched",allConditions.size());
        return allConditions
                .stream()
                .map(ConditionMapper.INSTANCE::toCondition)
                .collect(Collectors.toList());
    }

    public List<ServiceCondition> fetchAllServiceConditions(){
        List<ServiceConditionEntity> allServiceConditions = serviceConditionRepository.findAll();
        log.info(">>> all {} [service conditions] successfully fetched",allServiceConditions.size());
        return allServiceConditions
                .stream()
                .map(ConditionMapper.INSTANCE::toServiceCondition)
                .collect(Collectors.toList());
    }

    public List<TerminalCondition> fetchAllTerminalConditions(){
        List<TerminalConditionEntity> allTerminalConditions = terminalConditionRepository.findAll();
        log.info(">>> all {} [terminal conditions] successfully fetched",allTerminalConditions.size());
        return allTerminalConditions
                .stream()
                .map(ConditionMapper.INSTANCE::toTerminalCondition)
                .collect(Collectors.toList());
    }

    public List<TerminalServiceCondition> fetchAllTerminalServiceConditions() {
        List<TerminalServiceConditionEntity> allServiceTerminalConditions = serviceTerminalConditionRepository.findAll();
        log.info(">>> all {} [service terminal conditions] successfully fetched",allServiceTerminalConditions.size());
        return allServiceTerminalConditions
                .stream()
                .map(ConditionMapper.INSTANCE::toTerminalServiceCondition)
                .collect(Collectors.toList());
    }

}
