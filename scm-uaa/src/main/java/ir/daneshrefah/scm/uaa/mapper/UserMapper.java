package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.common.service.terminal.TerminalService;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.uaa.repository.authentication.UserEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-09
 */
//@Mapper(uses = IntegrationService.class, injectionStrategy = InjectionStrategy.FIELD, componentModel = "spring")
@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "terminalCode", expression = "java(mapTerminalCode(entity))")
    @Mapping(source = "person", target = "person", qualifiedByName = "toPerson")
    User toModel(UserEntity entity);

    @Named("toPerson")
    default GeneralPerson toPerson(GeneralPersonEntity entity) {
        return PersonMapper.INSTANCE.toPerson(entity);
    }

    @Named("toPersonEntity")
    default GeneralPersonEntity toPersonEntity(GeneralPerson person) {
        return PersonMapper.INSTANCE.toPersonEntity(person);
    }

    List<User> toModels(Iterable<UserEntity> entities);

    default String mapTerminalCode(UserEntity entity) {
        if (null == entity) {
            return null;
        }
        return TerminalService.INSTANCE.findTerminalByLegacyId(entity.getTerminalId()).map(terminal -> terminal.getCode()).orElse(null);
    }

    default Integer mapTerminalId(User user) {
        if (null == user) {
            return null;
        }
//        if (null != user.getTerminalId()) {
//            return user.getTerminalId();
//        }
        return TerminalService.INSTANCE.findTerminalByCode(user.getTerminalCode()).map(terminal -> terminal.getLegacyTerminalId().intValue()).orElse(null);
    }

    @Mapping(target = "terminalId", expression = "java(mapTerminalId(user))")
    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonEntity")
    UserEntity toEntity(User user);

}
