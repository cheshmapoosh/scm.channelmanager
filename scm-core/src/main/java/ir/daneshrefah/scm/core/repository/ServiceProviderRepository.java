package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.ExternalServiceProviderEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface ServiceProviderRepository extends CrudRepository<ExternalServiceProviderEntity, String> {

}
