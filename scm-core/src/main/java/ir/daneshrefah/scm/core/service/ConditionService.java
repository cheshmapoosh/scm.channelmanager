package ir.daneshrefah.scm.core.service;


import ir.daneshrefah.scm.core.entity.condition.*;
import ir.daneshrefah.scm.core.model.condition.ServiceCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalServiceCondition;
import ir.daneshrefah.scm.core.entity.condition.TerminalConditionEntity;
import ir.daneshrefah.scm.core.mapper.ConditionMapper;
import ir.daneshrefah.scm.core.repository.ServiceConditionRepository;
import ir.daneshrefah.scm.core.repository.TerminalConditionRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConditionService {

    private final ServiceConditionRepository serviceConditionRepository;
    private final TerminalConditionRepository terminalConditionRepository;
    private final TerminalServiceConditionRepository serviceTerminalConditionRepository;

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
