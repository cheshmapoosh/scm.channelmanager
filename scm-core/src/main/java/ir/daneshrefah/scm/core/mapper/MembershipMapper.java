package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.data.mapper.PersonMapper;
import ir.daneshrefah.scm.common.model.asset.AccountMembership;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.core.entity.asset.AccountMembershipEntity;
import ir.daneshrefah.scm.core.entity.asset.MembershipEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
@Mapper
public interface MembershipMapper {

    MembershipMapper INSTANCE = Mappers.getMapper(MembershipMapper.class);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonModel")
    AccountMembership toModel(AccountMembershipEntity entity);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonEntity")
    AccountMembershipEntity toEntity(AccountMembership model);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonModel")
    Membership toModel(MembershipEntity entity);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonEntity")
    MembershipEntity toEntity(Membership model);

    List<Membership> toModels(List<MembershipEntity> entities);

    @Named("toPersonModel")
    default GeneralPerson toPersonModel(GeneralPersonEntity entity) {
        return PersonMapper.INSTANCE.toPerson(entity);
    }

    @Named("toPersonEntity")
    default GeneralPersonEntity toPersonEntity(GeneralPerson model) {
        return PersonMapper.INSTANCE.toPersonEntity(model);
    }

}
