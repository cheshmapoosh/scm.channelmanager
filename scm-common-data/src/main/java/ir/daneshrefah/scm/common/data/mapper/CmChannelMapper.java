package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.gateway.CmChannelEntity;
import ir.daneshrefah.scm.common.model.gateway.CmChannel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {AuthenticationMethodMapper.class})
public interface CmChannelMapper {
    CmChannelMapper INSTANCE = Mappers.getMapper(CmChannelMapper.class);

    CmChannel toModel(CmChannelEntity entity);

    CmChannelEntity toEntity(CmChannel model);
}
