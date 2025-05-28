package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.core.entity.condition.ConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.ServiceConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalServiceConditionEntity;
import ir.daneshrefah.scm.core.model.condition.ServiceCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalCondition;
import ir.daneshrefah.scm.core.model.condition.TerminalServiceCondition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING,
        uses = {ScmServiceMapper.class,TerminalServiceAccessMapper.class})
public interface ConditionMapper {

    @Mapping(target = "bypassIgnorable", ignore = true)
    Condition toCondition(ConditionEntity conditionEntity);
    @Named("toModel")
    Condition toModel(ConditionEntity conditionEntity);

    @Mapping(source = "service", target = "service", qualifiedByName = "toService")
    @Mapping(source = "condition", target = "condition", qualifiedByName = "toModel")
    ServiceCondition toServiceCondition(ServiceConditionEntity serviceConditionEntity);

    @Mapping(source = "terminal", target = "terminal")
    @Mapping(source = "condition", target = "condition", qualifiedByName = "toModel")
    TerminalCondition toTerminalCondition(TerminalConditionEntity conditionEntity);

    @Mapping(source = "terminalServiceAccess", target = "terminalServiceAccess")
    @Mapping(source = "condition", target = "condition", qualifiedByName = "toModel")
    TerminalServiceCondition toTerminalServiceCondition(TerminalServiceConditionEntity serviceConditionEntity);

}
