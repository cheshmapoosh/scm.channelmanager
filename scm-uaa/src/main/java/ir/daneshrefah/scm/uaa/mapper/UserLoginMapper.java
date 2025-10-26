package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.pwa.PwaLogin;
import ir.daneshrefah.scm.uaa.repository.activation.domain.PwaLoginEntity;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface UserLoginMapper {

    PwaLoginEntity toEntity(PwaLogin model);

    PwaLogin toModel(PwaLoginEntity entity);

}
