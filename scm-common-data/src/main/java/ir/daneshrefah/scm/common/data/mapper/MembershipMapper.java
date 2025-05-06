package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipEntity;
import ir.daneshrefah.scm.common.data.entity.person.GeneralPersonEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipDto;
import ir.daneshrefah.scm.common.model.asset.Membership;
import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Mapper
public interface MembershipMapper {

    MembershipMapper INSTANCE = Mappers.getMapper(MembershipMapper.class);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonModel")
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
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

    default MembershipDto toDto(Membership model) {
        if (Objects.isNull(model)) return null;
        MembershipDto membershipDto = new MembershipDto();
        membershipDto.setNickname(model.getNickname());
        membershipDto.setCustomerAccount(model.getCustomerAccount());
        membershipDto.setDefaultAccount(model.getDefaultAccount());
        membershipDto.setArchiveNumber(model.getArchiveNumber());
        membershipDto.setClose(model.getClose());
        membershipDto.setId(model.getId());
        membershipDto.setCreator(model.getCreator());
        membershipDto.setLastEditor(model.getLastEditor());
        membershipDto.setCreateDate(model.getCreateDate());
        membershipDto.setLastEditDate(model.getLastEditDate());
        return membershipDto;
    }

}
