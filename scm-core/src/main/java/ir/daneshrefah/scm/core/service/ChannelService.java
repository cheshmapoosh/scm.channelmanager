package ir.daneshrefah.scm.core.service;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentEntity;
import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.core.mapper.ChannelMapper;
import ir.daneshrefah.scm.core.mapper.ServiceComponentMapper;
import ir.daneshrefah.scm.core.repository.ChannelRepository;
import ir.daneshrefah.scm.plugin.api.model.component.ServiceComponent;
import ir.daneshrefah.scm.plugin.api.model.terminal.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
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
