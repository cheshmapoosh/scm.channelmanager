package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.repository.activation.domain.RegisterEntity;
import ir.daneshrefah.scm.uaa.domain.pwa.Register;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface RegisterMapper {

    Register toMoel(RegisterEntity entity);

    RegisterEntity toEntity(Register register);

}
