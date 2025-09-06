package ir.daneshrefah.scm.common.data.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.dto.service.EbServiceCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EbServiceMapper {

    EbService toModel(ServiceEntity entity);

    List<EbService> toModels(List<ServiceEntity> entities);

    ServiceEntity toEntity(EbService model);

    ServiceEntity toEntity(EbServiceCreateRequest model);
}
