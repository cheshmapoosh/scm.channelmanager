package ir.daneshrefah.scm.logging.mapper;

import ir.daneshrefah.scm.logging.entity.LogTraceEntity;
import ir.daneshrefah.scm.logging.model.LogTraceDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LogTraceMapper {

    LogTraceMapper INSTANCE = Mappers.getMapper(LogTraceMapper.class);

    LogTraceDetailResponse toModel(LogTraceEntity entity);
}
