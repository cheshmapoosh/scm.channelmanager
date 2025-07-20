package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.data.mapper.CmChannelMapper;
import ir.daneshrefah.scm.common.data.repository.gateway.CmChannelRepository;
import ir.daneshrefah.scm.common.dto.gateway.CmChannelService;
import ir.daneshrefah.scm.common.model.gateway.CmChannel;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class CmChannelServiceIml implements CmChannelService {
    private static final Map<Integer, CmChannel> CHANNEL_CACHE = new ConcurrentHashMap<>();
    private final CmChannelRepository channelRepository;

    @PostConstruct
    public void init() {
        channelRepository
                .findAll()
                .stream()
                .map(CmChannelMapper.INSTANCE::toModel)
                .forEach(channel -> CHANNEL_CACHE.put(channel.getId(), channel));
    }

    @Override
    public List<CmChannel> findAllChannels() {
        return new ArrayList<>(CHANNEL_CACHE.values());
    }

    @Override
    public Optional<CmChannel> findChannelById(Integer id) {
        return Optional.ofNullable(CHANNEL_CACHE.get(id));
    }

    @Override
    public Optional<CmChannel> findChannelByCode(String code) {
        return findAllChannels()
                .stream()
                .filter(channel -> channel.getCode().trim().equalsIgnoreCase(String.valueOf(StringUtils.trim(code))))
                .filter(channel -> Objects.isNull(channel.getParentId()))
                .findFirst();
    }
}
