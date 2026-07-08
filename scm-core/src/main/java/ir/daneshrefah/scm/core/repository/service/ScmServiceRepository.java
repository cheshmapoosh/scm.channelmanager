package ir.daneshrefah.scm.core.repository.service;

import ir.daneshrefah.scm.common.data.entity.asset.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScmServiceRepository extends JpaRepository<ServiceEntity, Short>, JpaSpecificationExecutor<ServiceEntity> {

    Optional<ServiceEntity> findByCode(String code);
}
