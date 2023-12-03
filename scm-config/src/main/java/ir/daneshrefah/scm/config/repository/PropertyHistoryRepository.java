package ir.daneshrefah.scm.config.repository;

import ir.daneshrefah.scm.config.model.entity.PropertyHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyHistoryRepository extends JpaRepository<PropertyHistoryEntity,Long> {
}
