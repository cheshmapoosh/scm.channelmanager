package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.data.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPerson")
    User toModel(UserEntity entity);

    @Named("toPerson")
    default GeneralPerson toService(GeneralPersonEntity entity) {
        return PersonMapper.INSTANCE.toPerson(entity);
    }
//    List<User> entitiesToModels(Iterable<UserEntity> entities);

//    UserEntity toEntity(User model);

}
