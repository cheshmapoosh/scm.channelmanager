package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.model.service.ServiceImplementationType;
import ir.daneshrefah.scm.core.entity.service.ScmServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Deprecated
public interface ServiceRepository extends JpaRepository<ScmServiceEntity, String> {

    @Query("SELECT s FROM ScmServiceEntity s WHERE " +
            "s.implementationType <> ir.daneshrefah.scm.common.model.service.ServiceImplementationType.PARENT")
    List<ScmServiceEntity> findCallableServiceList();

    List<ScmServiceEntity> findServiceListByImplementationType(ServiceImplementationType implementationType);

    Optional<ScmServiceEntity> findByCode(String code);

}
