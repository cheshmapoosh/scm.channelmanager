package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.person.*;
import ir.daneshrefah.scm.common.model.person.*;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Mapper(unmappedTargetPolicy =IGNORE, componentModel = SPRING, uses = {ChannelMapper.class})
public interface PersonMapper {

    @Named("toPerson")
    default GeneralPerson toPerson(GeneralPersonEntity entity) {
        if (entity instanceof CorporatePersonEntity corporatePersonEntity) {
            return toModel(corporatePersonEntity);
        } else if (entity instanceof GovernancePersonEntity governancePersonEntity) {
            return toModel(governancePersonEntity);
        } else if (entity instanceof BankPersonEntity bankPersonEntity) {
            return toModel(bankPersonEntity);
        } else if (entity instanceof TaminPersonEntity taminPersonEntity) {
            return toModel(taminPersonEntity);
        } else if (entity instanceof IndividualPersonEntity individualPersonEntity) {
            return toModel(individualPersonEntity);
        } else if (entity instanceof EmployeePersonEntity employeePersonEntity) {
            return toModel(employeePersonEntity);
        } else if (entity instanceof ClientPersonEntity clientPersonEntity) {
            return toModel(clientPersonEntity);
        }
        return null;
    }

    @Named("toPersonEntity")
    default GeneralPersonEntity toPersonEntity(GeneralPerson person) {
        if (person instanceof CorporatePerson corporatePerson) {
            return toEntity(corporatePerson);
        } else if (person instanceof GovernancePerson governancePerson) {
            return toEntity(governancePerson);
        } else if (person instanceof BankPerson bankPerson) {
            return toEntity(bankPerson);
        } else if (person instanceof TaminPerson taminPerson) {
            return toEntity(taminPerson);
        } else if (person instanceof IndividualPerson individualPerson) {
            return toEntity(individualPerson);
        } else if (person instanceof EmployeePerson employeePerson) {
            return toEntity(employeePerson);
        }else if (person instanceof ClientPerson clientPerson) {
            return toEntity(clientPerson);
        }
        return null;
    }

    CorporatePerson toModel(CorporatePersonEntity entity);

    GovernancePerson toModel(GovernancePersonEntity entity);

    BankPerson toModel(BankPersonEntity entity);

    TaminPerson toModel(TaminPersonEntity entity);

    IndividualPerson toModel(IndividualPersonEntity entity);

    EmployeePerson toModel(EmployeePersonEntity entity);

    ClientPerson toModel(ClientPersonEntity entity);

    default List<GeneralPerson> toModels(Iterable<GeneralPersonEntity> entities) {
        if (null == entities)
            return null;
        List<GeneralPerson> result = new ArrayList<>();
        for (GeneralPersonEntity entity : entities) {
            GeneralPerson service = toPerson(entity);
            result.add(service);
        }
        return result;
    }

    CorporatePersonEntity toEntity(CorporatePerson person);

    GovernancePersonEntity toEntity(GovernancePerson person);

    BankPersonEntity toEntity(BankPerson person);

    TaminPersonEntity toEntity(TaminPerson person);

    IndividualPersonEntity toEntity(IndividualPerson person);

    EmployeePersonEntity toEntity(EmployeePerson person);

    ClientPersonEntity toEntity(ClientPerson person);

}
