package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.pwa.UserActivation;
import ir.daneshrefah.scm.uaa.repository.activation.domain.UserActivationEntity;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING, uses = {DeviceClientMapper.class})
public interface UserActivationMapper {

    UserActivationEntity toEntity(UserActivation userActivation);

    UserActivation toModel(UserActivationEntity userActivationEntity);

}
