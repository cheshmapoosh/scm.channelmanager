package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.AbstractExternalServiceProviderEntity;
import ir.daneshrefah.scm.core.entity.service.RestExternalServiceProviderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-20
 */
@Repository
public interface ServiceProviderRepository extends CrudRepository<AbstractExternalServiceProviderEntity, String> {
    Optional<AbstractExternalServiceProviderEntity> findByCode(String providerCode);

    @Query("select o from RestExternalServiceProviderEntity o")
    List<RestExternalServiceProviderEntity> findAllRestProviders();
}
