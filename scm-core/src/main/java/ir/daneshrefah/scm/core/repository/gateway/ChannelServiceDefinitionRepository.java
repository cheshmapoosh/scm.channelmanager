package ir.daneshrefah.scm.core.repository.gateway;

import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChannelServiceDefinitionRepository extends JpaRepository<ChannelServiceDefinitionEntity, String> {
    Optional<List<ChannelServiceDefinitionEntity>> findByChannelServiceAccess_IdAndGatewayChannel_Id(Long channelServiceAccessId, @Size(max = 36) String gatewayChannelId);
}