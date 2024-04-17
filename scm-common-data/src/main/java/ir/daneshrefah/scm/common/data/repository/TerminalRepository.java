package ir.daneshrefah.scm.common.data.repository;

import ir.daneshrefah.scm.common.data.entity.terminal.TerminalEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TerminalRepository extends CrudRepository<TerminalEntity, String> {
    Optional<TerminalEntity> findByCode(String code);
}
