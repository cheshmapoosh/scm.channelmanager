package ir.daneshrefah.scm.core.mapper;

import ir.daneshrefah.scm.common.model.asset.AssetProvider;
import ir.daneshrefah.scm.core.entity.asset.AssetProviderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

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

    AssetProvider toModel(AssetProviderEntity entity);
    AssetProviderEntity toEntity(AssetProvider model);

    List<AssetProvider> toModels(Iterable<AssetProviderEntity> entities);

}
