package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefinitionRepository extends JpaRepository<DefinitionEntity, String>, JpaSpecificationExecutor<DefinitionEntity> {
    Optional<DefinitionEntity> findByName(String name);
}
