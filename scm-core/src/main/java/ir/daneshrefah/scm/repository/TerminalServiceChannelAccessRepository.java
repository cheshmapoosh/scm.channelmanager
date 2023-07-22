package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.terminal.TerminalEntity;
import ir.daneshrefah.scm.entity.terminal.TerminalServiceChannelAccessEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TerminalServiceChannelAccessRepository extends CrudRepository<TerminalServiceChannelAccessEntity, String> {

    public List<TerminalServiceChannelAccessEntity> findAllByChannelEntityId(String channelId);

}
