package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.common.data.entity.TerminalEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TerminalRepository extends CrudRepository<TerminalEntity, String> {

}
