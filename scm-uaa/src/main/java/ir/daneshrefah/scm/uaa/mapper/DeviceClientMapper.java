package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.pwa.DeviceClient;
import ir.daneshrefah.scm.uaa.repository.activation.domain.DeviceClientEntity;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface DeviceClientMapper {

    DeviceClient toModel(DeviceClientEntity deviceClientEntity);

    DeviceClientEntity toEntity(DeviceClient deviceClient);

}
