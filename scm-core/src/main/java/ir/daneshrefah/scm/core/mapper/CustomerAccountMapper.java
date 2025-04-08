package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.asset.CustomerAccount;
import ir.daneshrefah.scm.common.data.entity.asset.CustomerAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CustomerAccountMapper {

    CustomerAccountMapper INSTANCE = Mappers.getMapper(CustomerAccountMapper.class);

    CustomerAccountEntity toEntity(CustomerAccount model);

    @Mapping(target = "creator" ,ignore = true)
    @Mapping(target = "createDate" ,ignore = true)
    @Mapping(target = "lastEditor" ,ignore = true)
    @Mapping(target = "lastEditDate" ,ignore = true)
    CustomerAccount toModel(CustomerAccountEntity entity);
}
