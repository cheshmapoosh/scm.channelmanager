package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.data.mapper.ChannelMapper;
import ir.daneshrefah.scm.common.data.repository.gateway.ChannelRepository;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMapper channelMapper;

    @Override
    public Optional<Channel> findChannelByCode(String code) {
        return channelRepository
                .findByCode(code)
                .map(channelMapper::toModel);
    }
}
