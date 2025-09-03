package ir.daneshrefah.scm.common.data.repository.assets;

import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChannelServiceAccessRepository extends JpaRepository<ChannelServiceAccessEntity, Long> {

    Optional<List<ChannelServiceAccessEntity>> findAllByChannelId(Short channelId);

    @Query("select o from ChannelServiceAccessEntity o where o.channel.id = :channelId and o.active = true and o.service.publish = true ")
    List<ChannelServiceAccessEntity> findByChannelId(@Param("channelId") Short channelId);

    @Query("select o from ChannelServiceAccessEntity o where o.service.id = :serviceId")
    List<ChannelServiceAccessEntity> findByServiceId(@Param("serviceId") Long serviceId);
}
