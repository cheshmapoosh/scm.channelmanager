package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.model.gateway.ProtocolType;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import ir.daneshrefah.scm.core.mapper.gateway.GatewayMapper;
import ir.daneshrefah.scm.core.repository.gateway.GatewayChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GatewayServiceImpl implements GatewayService {
    private final GatewayChannelRepository gatewayChannelRepository;
    private final GatewayMapper gatewayMapper;
    @Override
    public GatewayChannel findRestGatewayChannelByCode(String code) {
        Optional<GatewayChannelEntity> routeChannelEntityOptional = gatewayChannelRepository
                .findByCodeAndProtocolType(code, ProtocolType.REST);

        return routeChannelEntityOptional.map(gatewayMapper::toDto).orElse(null);
    }
}
