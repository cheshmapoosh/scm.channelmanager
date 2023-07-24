package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends CrudRepository<ChannelEntity, String> {

}
