package ir.daneshrefah.scm.common.data.service.assets;

import ir.daneshrefah.scm.common.constant.TerminalType;
import ir.daneshrefah.scm.common.data.mapper.ChannelServiceAccessMapper;
import ir.daneshrefah.scm.common.data.repository.assets.ChannelServiceAccessRepository;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ChannelServiceAccessService {

    private static final Map<TerminalType, List<ChannelServiceAccess>> CACHE = new ConcurrentHashMap<>();
    private final ChannelServiceAccessRepository channelServiceAccessRepository;

    public List<ChannelServiceAccess> findAllByTerminalType(TerminalType terminalType) {
        return CACHE.computeIfAbsent(terminalType, key -> channelServiceAccessRepository
                .findByChannelId(terminalType.getLegacyTerminalId())
                .stream()
                .map(ChannelServiceAccessMapper.INSTANCE::toModel)
                .toList());
    }

}
