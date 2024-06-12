package ir.daneshrefah.scm.process.model.mapper;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.process.model.IssuerUser;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper  INSTANCE = Mappers.getMapper(UserMapper.class);
    IssuerUser toUserMapper(GeneralPerson generalPerson);
}
