package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.common.model.person.CorporatePerson;
import ir.daneshrefah.scm.uaa.common.model.person.EmployeePerson;
import ir.daneshrefah.scm.uaa.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.common.model.person.IndividualPerson;
import ir.daneshrefah.scm.uaa.repository.authentication.CorporatePersonEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.EmployeePersonEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.GeneralPersonEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.IndividualPersonEntity;
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

    CorporatePerson toModel(CorporatePersonEntity entity);
    IndividualPerson toModel(IndividualPersonEntity entity);
    EmployeePerson toModel(EmployeePersonEntity entity);
//    List<User> entitiesToModels(Iterable<UserEntity> entities);

//    UserEntity toEntity(User model);

}
