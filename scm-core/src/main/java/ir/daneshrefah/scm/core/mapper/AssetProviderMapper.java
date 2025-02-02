package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.common.model.service.Service;
import ir.daneshrefah.scm.core.entity.asset.AssetProviderEntity;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Objects;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-06
 */
@Mapper
public interface AssetProviderMapper {
    AssetProviderMapper INSTANCE = Mappers.getMapper(AssetProviderMapper.class);

    @Mapping( target = "service", ignore = true)
    @Mapping(target = "creator" ,ignore = true)
    @Mapping(target = "createDate" ,ignore = true)
    @Mapping(target = "lastEditor" ,ignore = true)
    @Mapping(target = "lastEditDate" ,ignore = true)
    AssetProvider toModel(AssetProviderEntity entity);

//    @Mapping(source = "service.id", target = "serviceId") //TODO UNCOMMIT NEXT REALISES
    AssetProviderEntity toEntity(AssetProvider model);

}
