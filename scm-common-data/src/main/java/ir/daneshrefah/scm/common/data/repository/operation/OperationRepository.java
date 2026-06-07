package ir.daneshrefah.scm.common.data.repository.operation;

import ir.daneshrefah.scm.common.data.entity.operation.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<OperationEntity, String> , JpaSpecificationExecutor<OperationEntity> {
  List<OperationEntity> findAllByActive(Boolean active);

  List<OperationEntity> findByNameInAndActiveTrue(Collection<String> names);
}
