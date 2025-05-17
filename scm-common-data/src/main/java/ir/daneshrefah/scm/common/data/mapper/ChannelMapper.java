package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.gateway.ChannelEntity;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {AuthenticationMethodMapper.class})
public interface ChannelMapper {
    ChannelEntity toEntity(Channel channel);

    Channel toDto(ChannelEntity channelEntity);
}
