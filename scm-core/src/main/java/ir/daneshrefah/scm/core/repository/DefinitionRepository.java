package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DefinitionRepository extends JpaRepository<DefinitionEntity, String> {
    Page<DefinitionEntity> findAllByTypeIn(List<DefinitionType> types, Pageable pageable);
}
