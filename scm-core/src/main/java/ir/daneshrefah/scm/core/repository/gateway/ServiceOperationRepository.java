package ir.daneshrefah.scm.core.repository.gateway;

import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceOperationRepository extends JpaRepository<ServiceOperationEntity, String> {
    List<ServiceOperationEntity> findAllByService_Id(Short id);
}
