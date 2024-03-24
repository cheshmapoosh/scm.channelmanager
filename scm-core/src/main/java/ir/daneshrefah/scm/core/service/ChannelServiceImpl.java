package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.service.ChannelInfoRequest;
import ir.daneshrefah.scm.common.service.ChannelService;
import ir.daneshrefah.scm.core.mapper.ChannelMapper;
import ir.daneshrefah.scm.core.repository.ChannelRepository;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ChannelServiceImpl implements ChannelService {

    @Autowired
    private final ChannelRepository channelRepository;
    private List<Channel> channels;

    @Override
    public List<Channel> findAllChannels() {
        if (null == channels) {
            channels = ChannelMapper.INSTANCE.entitiesToModels(channelRepository.findAll());
        }
        return channels;
    }

    @Override
    public Optional<Channel> findChannelById(String id) {
        if (StringUtils.isEmpty(id)) {
            return Optional.empty();
        }
        return findAllChannels().stream().filter(channel -> id.equals(channel.getId())).findFirst();
    }

    @Override
    public Optional<Channel> findChannelByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return Optional.empty();
        }
        return findAllChannels().stream().filter(channel -> code.equals(channel.getCode())).findFirst();
    }

    @Override
    public PagedResponseData<Channel> findPagedChannels(ChannelInfoRequest request) {
        List<Channel> channelList = findAllChannels().stream()
                .filter(channel -> null == request || null == request.getCode() || request.getCode().equals(channel.getCode()))
                .filter(channel -> null == request || null == request.getTerminalCode() || request.getTerminalCode().equals(channel.getTerminal().getCode()))
                .filter(channel -> null == request || null == request.getProtocol() || request.getProtocol().equals(channel.getProtocol()))
                .collect(Collectors.toList());
        return new PagedResponseData<>(request, channelList);
    }

}
