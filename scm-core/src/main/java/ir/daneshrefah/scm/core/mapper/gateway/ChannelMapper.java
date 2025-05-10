package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.core.entity.gateway.ChannelEntity;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {AuthenticationMethodMapper.class})
public interface ChannelMapper {
    ChannelMapper INSTANCE = Mappers.getMapper(ChannelMapper.class);

    ChannelEntity toEntity(Channel channel);

    Channel toDto(ChannelEntity channelEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    ChannelEntity partialUpdate(Channel channel, @MappingTarget ChannelEntity channelEntity);
}