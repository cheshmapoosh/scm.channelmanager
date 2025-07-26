package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.EbServiceEntity;
import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.data.entity.asset.EbServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EbServiceMapper {
    EbServiceMapper INSTANCE = Mappers.getMapper(EbServiceMapper.class);

    EbService toModel(EbServiceEntity entity);

    EbServiceEntity toEntity(EbService model);
}
