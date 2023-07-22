package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.component.ServiceComponentEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceComponentRepository extends CrudRepository<ServiceComponentEntity, String> {

    public List<ServiceComponentEntity> findListByServiceComponentProviderId(String serviceComponentProviderId);

}
