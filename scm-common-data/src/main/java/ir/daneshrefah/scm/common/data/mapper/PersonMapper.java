package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.person.*;
import ir.daneshrefah.scm.common.model.person.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-10
 */
@Mapper
public interface PersonMapper {

    PersonMapper INSTANCE = Mappers.getMapper(PersonMapper.class);

    @Named("toPerson")
    default GeneralPerson toPerson(GeneralPersonEntity entity) {
        if (entity instanceof CorporatePersonEntity) {
            return toModel((CorporatePersonEntity) entity);
        } else if (entity instanceof GovernancePersonEntity) {
            return toModel((GovernancePersonEntity) entity);
        } else if (entity instanceof BankPersonEntity) {
            return toModel((BankPersonEntity) entity);
        } else if (entity instanceof TaminPersonEntity) {
            return toModel((TaminPersonEntity) entity);
        } else if (entity instanceof IndividualPersonEntity) {
            return toModel((IndividualPersonEntity) entity);
        } else if (entity instanceof EmployeePersonEntity) {
            return toModel((EmployeePersonEntity) entity);
        }
        return null;
    }

    @Named("toPersonEntity")
    default GeneralPersonEntity toPersonEntity(GeneralPerson person) {
        if (person instanceof CorporatePerson) {
            return toEntity((CorporatePerson) person);
        } else if (person instanceof GovernancePerson) {
            return toEntity((GovernancePerson) person);
        } else if (person instanceof BankPerson) {
            return toEntity((BankPerson) person);
        } else if (person instanceof TaminPerson) {
            return toEntity((TaminPerson) person);
        } else if (person instanceof IndividualPerson) {
            return toEntity((IndividualPerson) person);
        } else if (person instanceof EmployeePerson) {
            return toEntity((EmployeePerson) person);
        }
        return null;
    }

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
    CorporatePerson toModel(CorporatePersonEntity entity);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
    GovernancePerson toModel(GovernancePersonEntity entity);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
    BankPerson toModel(BankPersonEntity entity);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
    TaminPerson toModel(TaminPersonEntity entity);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
    IndividualPerson toModel(IndividualPersonEntity entity);

    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
    EmployeePerson toModel(EmployeePersonEntity entity);

    default List<GeneralPerson> toModels(Iterable<GeneralPersonEntity> entities) {
        if (null == entities)
            return null;
        List<GeneralPerson> result = new ArrayList<>();
        for (Iterator<GeneralPersonEntity> iterator = entities.iterator(); iterator.hasNext(); ) {
            GeneralPersonEntity entity = iterator.next();
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

}
