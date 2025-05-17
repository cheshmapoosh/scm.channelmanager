package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {ChannelMapper.class})
public interface GatewayMapper {
    GatewayChannelEntity toEntity(GatewayChannel gatewayChannel);

    GatewayChannel toDto(GatewayChannelEntity gatewayChannelEntity);
}