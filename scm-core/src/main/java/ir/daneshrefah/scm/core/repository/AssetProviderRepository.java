package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.asset.AssetProviderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-07-06
 */
@Repository
public interface AssetProviderRepository extends CrudRepository<AssetProviderEntity, Integer> {

}
