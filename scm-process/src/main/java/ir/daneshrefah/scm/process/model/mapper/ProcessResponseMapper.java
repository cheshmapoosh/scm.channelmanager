package ir.daneshrefah.scm.process.model.mapper;

import ir.daneshrefah.scm.process.service.dto.process.ProcessStartResponse;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProcessResponseMapper {
    ProcessResponseMapper  INSTANCE = Mappers.getMapper(ProcessResponseMapper.class);
    ProcessStartResponse toProcessResponse(ProcessInstance processInstance);
}
