package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.MembershipEntity;
import ir.daneshrefah.scm.common.dto.asset.MembershipDto;
import ir.daneshrefah.scm.common.model.asset.Membership;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE,
        componentModel = SPRING
        , uses = {PersonMapper.class})
public interface MembershipMapper {


    @Mapping(source = "person", target = "person", qualifiedByName = "toPerson")
    @Mapping(target = "creator", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    @Mapping(target = "lastEditor", ignore = true)
    @Mapping(target = "lastEditDate", ignore = true)
    Membership toModel(MembershipEntity entity);

    @Mapping(source = "person", target = "person", qualifiedByName = "toPersonEntity")
    MembershipEntity toEntity(Membership model);

    List<Membership> toModels(List<MembershipEntity> entities);

}
