package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import ir.daneshrefah.scm.common.model.terminal.Channel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.stream.Collectors;

@Mapper
public interface ChannelMapper {
    ChannelMapper INSTANCE = Mappers.getMapper(ChannelMapper.class);

    Channel toModel(ChannelEntity entity);

    List<Channel> entitiesToModels(Iterable<ChannelEntity> entities);

    default Page<Channel> pageEntityToPageModel(Page<ChannelEntity> page) {
        List<Channel> items = page.getContent()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());

        return new PageImpl<>(items, page.getPageable(), page.getTotalElements());
    }
}
