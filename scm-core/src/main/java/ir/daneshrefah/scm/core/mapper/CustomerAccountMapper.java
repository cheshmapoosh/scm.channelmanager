package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.asset.CustomerAccount;
import ir.daneshrefah.scm.common.data.entity.asset.CustomerAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface CustomerAccountMapper {

    CustomerAccountEntity toEntity(CustomerAccount model);

    CustomerAccount toModel(CustomerAccountEntity entity);
}
