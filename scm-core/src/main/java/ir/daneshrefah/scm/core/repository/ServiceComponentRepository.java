package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.component.ServiceComponentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceComponentRepository extends CrudRepository<ServiceComponentEntity, String> {

    public List<ServiceComponentEntity> findListByServiceComponentProviderId(String serviceComponentProviderId);

}
