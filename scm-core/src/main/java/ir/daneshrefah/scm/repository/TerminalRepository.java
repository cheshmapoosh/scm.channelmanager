package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.terminal.TerminalEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalRepository extends CrudRepository<TerminalEntity, String> {

}
