package ir.daneshrefah.scm.core.repository.operation;

import ir.daneshrefah.scm.core.entity.operation.OperationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<OperationEntity, String> {
  List<OperationEntity> findAllByActive(Boolean active);
}