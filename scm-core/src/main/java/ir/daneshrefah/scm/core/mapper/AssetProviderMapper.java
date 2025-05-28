package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.data.entity.asset.AssetProviderEntity;
import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.core.mapper.definition.DefinitionMapper;
import org.mapstruct.Mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.ReportingPolicy.IGNORE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-06
 */
@Mapper(unmappedTargetPolicy = IGNORE, componentModel = SPRING)
public interface AssetProviderMapper {

    AssetProvider toModel(AssetProviderEntity entity);

    //    @Mapping(source = "service.id", target = "serviceId") //TODO UNCOMMIT NEXT REALISES
    AssetProviderEntity toEntity(AssetProvider model);

}
