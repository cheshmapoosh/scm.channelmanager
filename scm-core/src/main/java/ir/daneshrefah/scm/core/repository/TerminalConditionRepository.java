package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.condition.TerminalConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TerminalConditionRepository extends JpaRepository<TerminalConditionEntity,String> {
}
