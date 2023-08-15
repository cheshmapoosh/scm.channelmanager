package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.core.mapper.ChannelMapper;
import ir.daneshrefah.scm.core.repository.ChannelRepository;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {

    @Autowired
    ChannelRepository channelRepository;

    public List<Channel> findAllChannelList() {
        Iterable<ChannelEntity> channelEntities = channelRepository
                .findAll();
        return ChannelMapper.INSTANCE.entitiesToModels(channelEntities);
    }

    public Page<Channel> findPagedChannelList(Pageable pageable) {
        Page<ChannelEntity> channelEntities = channelRepository
                .findAll(pageable);
        return ChannelMapper.INSTANCE.pageEntityToPageModel(channelEntities);
    }

}
