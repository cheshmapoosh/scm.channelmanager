package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE,
        componentModel = SPRING,
        uses = {ChannelMapper.class})
public interface ChannelServiceAccessMapper {
    @Mapping(source = "service", target = "service")
    ChannelServiceAccessEntity toEntity(ChannelServiceAccess channelServiceAccess);


    @Mapping(source = "service", target = "service")
    ChannelServiceAccess toModel(ChannelServiceAccessEntity channelServiceAccessEntity);

    List<ChannelServiceAccess> toModel(List<ChannelServiceAccessEntity> channelServiceAccessEntity);
}
