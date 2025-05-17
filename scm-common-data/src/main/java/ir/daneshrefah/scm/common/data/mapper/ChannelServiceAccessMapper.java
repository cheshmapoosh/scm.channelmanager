package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {ChannelMapper.class})
public interface ChannelServiceAccessMapper {
    @Mapping(source = "service", target = "service")
    ChannelServiceAccessEntity toEntity(ChannelServiceAccess channelServiceAccess);

    @Mapping(source = "service", target = "service")
    ChannelServiceAccess toDto(ChannelServiceAccessEntity channelServiceAccessEntity);
}
