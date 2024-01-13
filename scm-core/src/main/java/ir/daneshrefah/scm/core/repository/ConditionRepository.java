package ir.daneshrefah.scm.core.repository;


import ir.daneshrefah.scm.core.entity.condition.ConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConditionRepository extends JpaRepository<ConditionEntity, Long> {
}
