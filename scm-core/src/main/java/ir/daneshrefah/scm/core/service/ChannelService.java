package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.core.mapper.ChannelMapper;
import ir.daneshrefah.scm.core.repository.ChannelRepository;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChannelService {

    @Autowired
    ChannelRepository channelRepository;

    public List<Channel> findChannelList() {
        Iterable<ChannelEntity> channelEntities = channelRepository
                .findAll();
        return ChannelMapper.INSTANCE.entitiesToModels(channelEntities);
    }

}
