package ir.daneshrefah.scm.common.data.repository.distribution;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ir.daneshrefah.scm.common.data.entity.distribution.DistributedLockEntity;

@Repository
public interface DistributedLockRepository extends JpaRepository<DistributedLockEntity,String> {

}
