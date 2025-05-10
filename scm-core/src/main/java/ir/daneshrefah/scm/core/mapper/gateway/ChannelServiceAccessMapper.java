package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceAccess;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceAccessEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ChannelMapper.class})
public interface ChannelServiceAccessMapper {
    @Mapping(source = "service", target = "service")
    ChannelServiceAccessEntity toEntity(ChannelServiceAccess channelServiceAccess);

    @Mapping(source = "service", target = "service")
    ChannelServiceAccess toDto(ChannelServiceAccessEntity channelServiceAccessEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "service", target = "service")
    ChannelServiceAccessEntity partialUpdate(ChannelServiceAccess channelServiceAccess, @MappingTarget ChannelServiceAccessEntity channelServiceAccessEntity);
}