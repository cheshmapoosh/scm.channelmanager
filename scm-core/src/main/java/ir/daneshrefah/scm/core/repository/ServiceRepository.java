package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends CrudRepository<ServiceEntity, String> {

    @Query("SELECT s FROM ServiceEntity s WHERE " +
            "s.implementationType <> ir.daneshrefah.scm.common.model.service.ServiceImplementationType.PARENT")
    List<ServiceEntity> findCallableServiceList();

    List<ServiceEntity> findServiceListByImplementationType(ServiceImplementationType implementationType);

}
