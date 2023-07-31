package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceComponentRepository extends CrudRepository<ServiceComponentEntity, String> {

    public List<ServiceComponentEntity> findListByServiceComponentProviderEntityId(String serviceComponentProviderId);

    @Query("SELECT c FROM ServiceComponentEntity c WHERE c.code = :componentCode AND c.serviceComponentProviderEntity.code = :providerCode")
    ServiceComponentEntity findByCodeAndProviderCode(@Param("componentCode") String componentCode, @Param("providerCode") String providerCode);

}
