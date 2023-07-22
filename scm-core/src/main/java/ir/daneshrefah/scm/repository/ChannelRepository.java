package ir.daneshrefah.scm.repository;

import ir.daneshrefah.scm.entity.terminal.ChannelEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends CrudRepository<ChannelEntity, String> {

}
