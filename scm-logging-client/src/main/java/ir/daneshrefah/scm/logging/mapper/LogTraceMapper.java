package ir.daneshrefah.scm.logging.mapper;

import ir.daneshrefah.scm.common.data.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.model.logging.LogTraceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LogTraceMapper {

    LogTraceMapper INSTANCE = Mappers.getMapper(LogTraceMapper.class);

    LogTraceResponse toModel(LogTraceEntity entity);

    List<LogTraceResponse> toModelList(List<LogTraceEntity> entities);
}
