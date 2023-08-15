package ir.daneshrefah.scm.core.repository;

import ir.daneshrefah.scm.core.entity.terminal.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {

}
