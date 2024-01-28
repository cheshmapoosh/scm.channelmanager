package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.model.person.CorporatePerson;
import ir.daneshrefah.scm.common.data.model.person.EmployeePerson;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.data.model.person.IndividualPerson;
import ir.daneshrefah.scm.common.data.entity.person.CorporatePersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.EmployeePersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.entity.person.IndividualPersonEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

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
        } else if (person instanceof IndividualPerson) {
            return toEntity((IndividualPerson) person);
        } else if (person instanceof EmployeePerson) {
            return toEntity((EmployeePerson) person);
        }
        return null;
    }

    CorporatePerson toModel(CorporatePersonEntity entity);
    IndividualPerson toModel(IndividualPersonEntity entity);
    EmployeePerson toModel(EmployeePersonEntity entity);

    CorporatePersonEntity toEntity(CorporatePerson person);
    IndividualPersonEntity toEntity(IndividualPerson person);
    EmployeePersonEntity toEntity(EmployeePerson person);

}
