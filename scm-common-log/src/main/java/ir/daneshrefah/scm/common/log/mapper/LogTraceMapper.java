package ir.daneshrefah.scm.common.log.mapper;

import ir.daneshrefah.scm.common.log.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.log.model.LogTraceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.Date;
import java.util.List;

@Mapper
public interface LogTraceMapper {

    LogTraceMapper INSTANCE = Mappers.getMapper(LogTraceMapper.class);


    @Mappings({
            @Mapping(source = "logPrimaryKey.spanId", target = "spanId"),
            @Mapping(source = "logPrimaryKey.traceId", target = "traceId"),
            @Mapping(target = "durationMillis", expression = "java(calculateDuration(entity.getStartTime(), entity.getEndTime()))")
    })
    LogTraceResponse toModel(LogTraceEntity entity);

    default List<LogTraceResponse> toModelList(List<LogTraceEntity> entities) {
        if (null == entities || entities.isEmpty()) return null;
        return entities.stream().map(this::toModel).toList();
    }

    default Long calculateDuration(Date startTime, Date endTime) {
        if (startTime == null || endTime == null) return null;
        return endTime.getTime() - startTime.getTime();
    }
}
