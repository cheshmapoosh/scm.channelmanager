package ir.daneshrefah.scm.core.repository.gateway;

import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelServiceAccessRepository extends JpaRepository<ChannelServiceAccessEntity, Long> {
    Optional<List<ChannelServiceAccessEntity>> findAllByChannelId(Short channelId);
}
