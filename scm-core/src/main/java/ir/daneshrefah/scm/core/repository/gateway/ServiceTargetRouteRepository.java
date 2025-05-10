package ir.daneshrefah.scm.core.repository.gateway;

import ir.daneshrefah.scm.core.entity.gateway.ServiceOperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ServiceTargetRouteRepository extends JpaRepository<ServiceOperationEntity, String> , JpaSpecificationExecutor<ServiceOperationEntity> {
  }