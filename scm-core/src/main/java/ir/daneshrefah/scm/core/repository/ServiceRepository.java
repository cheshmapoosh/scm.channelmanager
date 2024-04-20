package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.core.entity.service.ServiceEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceRepository extends CrudRepository<ServiceEntity, String> {

    @Query("SELECT s FROM ServiceEntity s WHERE " +
            "s.implementationType <> ir.daneshrefah.scm.common.model.service.ServiceImplementationType.PARENT")
    List<ServiceEntity> findCallableServiceList();

    List<ServiceEntity> findServiceListByImplementationType(ServiceImplementationType implementationType);

    Optional<ServiceEntity> findByCode(String code);

    @Transactional
    int deleteByIdAndLastEditDate(String id, LocalDateTime lastEditDate);

}
