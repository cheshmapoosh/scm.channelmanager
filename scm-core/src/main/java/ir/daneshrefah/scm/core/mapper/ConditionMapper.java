package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.condition.Condition;
import ir.daneshrefah.scm.common.model.condition.ServiceCondition;
import ir.daneshrefah.scm.common.model.condition.TerminalCondition;
import ir.daneshrefah.scm.common.model.condition.TerminalServiceCondition;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.common.model.terminal.Terminal;
import ir.daneshrefah.scm.common.model.terminal.TerminalServiceAccess;
import ir.daneshrefah.scm.core.entity.condition.ConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.ServiceConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalConditionEntity;
import ir.daneshrefah.scm.core.entity.condition.TerminalServiceConditionEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConditionMapper {
    ConditionMapper INSTANCE = Mappers.getMapper(ConditionMapper.class);

    Condition toCondition(ConditionEntity conditionEntity);

    @Mapping(source = "serviceEntity", target = "service", qualifiedByName = "mapService")
    @Mapping(source = "conditionEntity",target = "condition",qualifiedByName = "mapCondition")
    ServiceCondition toServiceCondition(ServiceConditionEntity serviceConditionEntity);

    @Mapping(source = "terminalEntity", target = "terminal", qualifiedByName = "mapTerminal")
    @Mapping(source = "conditionEntity",target = "condition",qualifiedByName = "mapCondition")
    TerminalCondition toTerminalCondition(TerminalConditionEntity conditionEntity);

    @Mapping(source = "terminalServiceAccessEntity", target = "terminalServiceAccess", qualifiedByName = "mapTerminalService")
    @Mapping(source = "conditionEntity",target = "condition",qualifiedByName = "mapCondition")
    TerminalServiceCondition toTerminalServiceCondition(TerminalServiceConditionEntity serviceConditionEntity);

    @Named("mapService")
    default Service mapService(ServiceEntity entity) {
        return ServiceMapper.INSTANCE.toService(entity);
    }

    @Named("mapTerminal")
    default Terminal mapTerminal(TerminalEntity entity) {
        return TerminalMapper.INSTANCE.toModel(entity);
    }
    @Named("mapTerminalService")
    default TerminalServiceAccess mapTerminalService(TerminalServiceAccessEntity entity) {
        return TerminalServiceAccessMapper.INSTANCE.toModel(entity);
    }

    @Named("mapCondition")
    default Condition mapCondition(ConditionEntity conditionEntity) {return  ConditionMapper.INSTANCE.toCondition(conditionEntity);}
}
