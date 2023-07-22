package ir.daneshrefah.scm.service;

import ir.daneshrefah.scm.common.model.service.JavaServiceImplementation;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import ir.daneshrefah.scm.common.model.terminal.RestChannel;
import ir.daneshrefah.scm.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.repository.ChannelRepository;
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
        Iterable<ChannelEntity> channelEntities = channelRepository.findAll();
        List<Channel> channelList = new ArrayList<>();
        for (Iterator<ChannelEntity> iterator = channelEntities.iterator(); iterator.hasNext(); ) {
            ChannelEntity channelEntity = iterator.next();
            Channel channel = null;
            switch (channelEntity.getProtocol()) {
                case REST:
                    channel = new RestChannel();
                    channel.setId(channelEntity.getId());
                    channel.setCode(channelEntity.getCode());
                    channel.setTitle(channelEntity.getTitle());
                    ((RestChannel) channel).setContext(channelEntity.getRestProtocolContext());
                    ((RestChannel) channel).setPort(channelEntity.getRestProtocolPort());
                    break;
                case JMS:
                    JavaServiceImplementation javaImplementation = new JavaServiceImplementation();
                    break;
                case RMI:
                    System.out.println("It's Wednesday.");
                    break;
                case JAVA:
                    System.out.println("It's Wednesday.");
                    break;
                default:
                    System.out.println("Invalid day of the week.");
                    break;
            }
            channelList.add(channel);
        }
        return channelList;
    }

}
