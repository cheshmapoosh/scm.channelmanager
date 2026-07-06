package ir.daneshrefah.scm.provider.task.repository;

import ir.daneshrefah.scm.provider.task.constant.DefinitionTypeEnum;
import ir.daneshrefah.scm.provider.task.constant.ExecutionMethodTypeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessCodeEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessNameEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessTaskDefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProcessTaskDefinitionRepository extends JpaRepository<ProcessTaskDefinitionEntity, Long> {

    Optional<ProcessTaskDefinitionEntity> findByProcessNameAndExecutionMethodTypeAndDefinitionType(String processName, Integer executionMethodType, Integer definitionType);

    Optional<ProcessTaskDefinitionEntity> findByProcessNameAndExecutionMethodTypeAndDefinitionTypeAndProcessCode(ProcessNameEnum processName, ExecutionMethodTypeEnum executionMethodType, DefinitionTypeEnum definitionType, ProcessCodeEnum processCode);
}
