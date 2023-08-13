package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ChannelMapper {
    ChannelMapper INSTANCE = Mappers.getMapper(ChannelMapper.class);

    Channel toModel(ChannelEntity entity);

    List<Channel> entitiesToModels(Iterable<ChannelEntity> entities);

}
