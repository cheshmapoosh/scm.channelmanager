package ir.daneshrefah.scm.common.data.repository.gateway;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategoryEntity,Short> {
}
