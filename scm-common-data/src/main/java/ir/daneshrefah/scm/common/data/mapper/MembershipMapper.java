package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipDto;
import ir.daneshrefah.scm.common.model.asset.Membership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Objects;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
uses = {PersonMapper.class})
public interface MembershipMapper {


    @Mapping(source = "person", target = "person", qualifiedByName = "toPerson")
    Membership toModel(MembershipEntity entity);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonEntity")
    MembershipEntity toEntity(Membership model);

    List<Membership> toModels(List<MembershipEntity> entities);

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
