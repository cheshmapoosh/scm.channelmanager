package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.service.parameter.ParameterProviderRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterProviderRelationRepository extends JpaRepository<ParameterProviderRelationEntity,Long> {
}
