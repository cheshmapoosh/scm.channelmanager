package ir.daneshrefah.scm.core.service;


import ir.daneshrefah.scm.common.model.person.ServiceAccess;
import ir.daneshrefah.scm.core.entity.condition.ServiceConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalServiceConditionEntity;
import ir.daneshrefah.scm.core.entity.person.ServiceAccessEntity;
import ir.daneshrefah.scm.core.mapper.ConditionMapper;
import ir.daneshrefah.scm.core.mapper.ServiceAccessMapper;
import ir.daneshrefah.scm.core.model.condition.ServiceCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalServiceCondition;
import ir.daneshrefah.scm.core.repository.ServiceAccessRepository;
import ir.daneshrefah.scm.core.repository.ServiceConditionRepository;
import ir.daneshrefah.scm.core.repository.TerminalConditionRepository;
import ir.daneshrefah.scm.core.repository.TerminalServiceConditionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-14
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceAccessService {

    private final ServiceAccessRepository serviceAccessRepository;

    public List<ServiceAccess> findByPersonProfileId(String personProfileId) {
        List<ServiceAccessEntity> entities = serviceAccessRepository.findByPersonProfileId(personProfileId);
        return ServiceAccessMapper.INSTANCE.entitiesToModels(entities);
    }

}
