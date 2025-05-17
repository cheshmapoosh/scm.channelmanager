package ir.daneshrefah.scm.common.data.repository.gateway;

import ir.daneshrefah.scm.common.data.entity.gateway.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity,Integer> {
}
