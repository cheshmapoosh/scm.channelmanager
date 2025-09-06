package ir.daneshrefah.scm.core.services.gateway;

import ir.daneshrefah.scm.common.data.entity.gateway.ChannelEntity;
import ir.daneshrefah.scm.common.data.mapper.ChannelMapper;
import ir.daneshrefah.scm.common.data.repository.channel.ChannelRepository;
import ir.daneshrefah.scm.common.data.repository.channel.ChannelSpecification;
import ir.daneshrefah.scm.common.dto.channel.ChannelFindRequest;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.log.utils.PageableUtils;
import ir.daneshrefah.scm.common.model.gateway.Channel;
import ir.daneshrefah.scm.common.service.channel.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelServiceImpl implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ChannelMapper channelMapper;

    @Override
    public List<Channel> findAllChannels(ChannelFindRequest request) {
        Specification<ChannelEntity> specification = ChannelSpecification.toSpecification(request);
        Pageable pageable = PageableUtils.getPageable(request);
        return channelRepository.findAll(specification, pageable).stream().map(channelMapper::toModel).toList();
    }

    @Override
    public Optional<Channel> findChannelByCode(String code) {
        return channelRepository
                .findByCode(code)
                .map(channelMapper::toModel);
    }

    @Override
    public Channel findChannelById(Short id) {
        ChannelEntity channelEntity = channelRepository.findById(id).orElseThrow(() -> new NoMatchRecordFoundException("channel with id " + id + " not found"));
        return channelMapper.toModel(channelEntity);
    }
}
