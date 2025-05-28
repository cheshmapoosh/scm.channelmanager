package ir.daneshrefah.scm.core.mapper.gateway;

import ir.daneshrefah.scm.common.data.mapper.ChannelMapper;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import org.mapstruct.*;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy =IGNORE, componentModel = SPRING, uses = {ChannelMapper.class})
public interface GatewayChannelMapper {
    GatewayChannelEntity toEntity(GatewayChannel gatewayChannel);

    GatewayChannel toModel(GatewayChannelEntity gatewayChannelEntity);
}