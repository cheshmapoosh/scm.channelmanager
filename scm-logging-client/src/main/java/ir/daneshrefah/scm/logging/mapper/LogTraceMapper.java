package ir.daneshrefah.scm.logging.mapper;

import ir.daneshrefah.scm.common.data.entity.logging.LogTraceEntity;
import ir.daneshrefah.scm.common.model.logging.LogTraceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LogTraceMapper {

    LogTraceMapper INSTANCE = Mappers.getMapper(LogTraceMapper.class);


    @Mappings({
            @Mapping(source = "logPrimaryKey.spanId", target = "spanId"),
            @Mapping(source = "logPrimaryKey.traceId", target = "traceId")
    })
    LogTraceResponse toModel(LogTraceEntity entity);

    default List<LogTraceResponse> toModelList(List<LogTraceEntity> entities) {
        if (null == entities || entities.isEmpty()) return null;
        return entities.stream().map(this::toModel).toList();
    }
}
