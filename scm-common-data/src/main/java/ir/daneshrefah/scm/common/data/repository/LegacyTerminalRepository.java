package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.terminal.LegacyTerminalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegacyTerminalRepository extends JpaRepository<LegacyTerminalEntity,Long> {

    Optional<LegacyTerminalEntity> findByCodeAndParentId(String code,Long parentId);
}
