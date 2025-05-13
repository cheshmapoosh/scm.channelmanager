package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {CmChannelMapper.class, EbServiceMapper.class})
public interface ChannelServiceAccessMapper {
    ChannelServiceAccessMapper INSTANCE = Mappers.getMapper(ChannelServiceAccessMapper.class);
    ChannelServiceAccess toModel(ChannelServiceAccessEntity entity);

    ChannelServiceAccessEntity toEntity(ChannelServiceAccess model);
}
