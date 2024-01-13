package ir.daneshrefah.scm.core.repository;


import ir.daneshrefah.scm.core.entity.condition.ServiceConditionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceConditionRepository extends JpaRepository<ServiceConditionEntity, Long> {
}
