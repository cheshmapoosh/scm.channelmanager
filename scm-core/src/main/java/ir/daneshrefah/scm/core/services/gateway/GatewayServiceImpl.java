package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.gateway.GatewayChannel;
import ir.daneshrefah.scm.common.service.GatewayService;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import ir.daneshrefah.scm.core.mapper.gateway.GatewayChannelMapper;
import ir.daneshrefah.scm.core.repository.gateway.GatewayChannelRepository;
import ir.daneshrefah.scm.utils.validation.ValidationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GatewayServiceImpl implements GatewayService {

    private final GatewayChannelRepository gatewayChannelRepository;
    private final GatewayChannelMapper gatewayChannelMapper;

    @Override
    public GatewayChannel findGatewayChannelByName(String name) {
        Optional<GatewayChannelEntity> routeChannelEntityOptional = gatewayChannelRepository
                .findByName(name);

        return routeChannelEntityOptional.map(gatewayChannelMapper::toModel).orElse(null);
    }

    @Override
    public GatewayChannel findById(String id) {
        ValidationUtils.checkNull(id, () -> new NoMatchRecordFoundException("gatewayChannelId"));
        GatewayChannelEntity gatewayChannel = gatewayChannelRepository
                .findById(id).orElseThrow(() -> new NoMatchRecordFoundException("gatewayChannel"));
        return gatewayChannelMapper.toModel(gatewayChannel);
    }

    @Override
    public List<GatewayChannel> findAll() {
        return gatewayChannelRepository
                .findAll()
                .stream()
                .map(gatewayChannelMapper::toModel)
                .toList();
    }
}
