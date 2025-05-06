package ir.daneshrefah.scm.common.data.repository.assets;

import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelServiceAccessRepository extends JpaRepository<ChannelServiceAccessEntity, Long> {

    @Query("select o from ChannelServiceAccessEntity o where o.terminal.legacyTerminalId = :legacyTerminalId and o.active = true and o.ebService.publish = true ")
    List<ChannelServiceAccessEntity> findByLegacyTerminalId(@Param("legacyTerminalId") Long legacyTerminalId);
}
