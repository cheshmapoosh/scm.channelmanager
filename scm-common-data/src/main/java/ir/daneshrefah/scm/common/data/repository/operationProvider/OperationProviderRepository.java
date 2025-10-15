package ir.daneshrefah.scm.common.data.repository.operationProvider;

import ir.daneshrefah.scm.common.data.entity.operation.OperationProviderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationProviderRepository extends JpaRepository<OperationProviderEntity,String> {
}