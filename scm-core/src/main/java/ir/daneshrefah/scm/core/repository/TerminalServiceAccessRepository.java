package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.terminal.TerminalServiceAccessEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalServiceAccessRepository extends CrudRepository<TerminalServiceAccessEntity, String> {

    List<TerminalServiceAccessEntity> findAllByTerminalId(String terminalId);
    List<TerminalServiceAccessEntity> findAllByServiceId(String serviceId);

    Optional<TerminalServiceAccessEntity> findByTerminal_IdAndService_Id(String terminalId,String serviceId);
}
