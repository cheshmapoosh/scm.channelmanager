package ir.daneshrefah.scm.uaa.mapper;

import ir.daneshrefah.scm.uaa.domain.pwa.WhiteList;
import ir.daneshrefah.scm.uaa.repository.activation.domain.WhiteListEntity;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface WhiteListMapper {

    WhiteListEntity toEntity(WhiteList model);

    WhiteList toModel(WhiteListEntity entity);

}

