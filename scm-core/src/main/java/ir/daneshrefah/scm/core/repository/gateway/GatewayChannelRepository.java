package ir.daneshrefah.scm.core.repository.gateway;

import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import ir.daneshrefah.scm.core.entity.gateway.GatewayChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatewayChannelRepository extends JpaRepository<GatewayChannelEntity, String> {

    Optional<GatewayChannelEntity> findByName(String name);
}
