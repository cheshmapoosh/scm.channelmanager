package ir.daneshrefah.scm.log.mapper;


import ir.daneshrefah.scm.log.entity.LogTraceEntity;
import ir.daneshrefah.scm.log.model.LogTraceDetailResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LogTraceMapper {

    LogTraceMapper INSTANCE = Mappers.getMapper(LogTraceMapper.class);

    LogTraceDetailResponse toModel(LogTraceEntity entity);
}
