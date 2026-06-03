package ir.daneshrefah.scm.core.repository.gateway;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelServiceDefinitionRepository extends JpaRepository<ChannelServiceDefinitionEntity, String> {
    List<ChannelServiceDefinitionEntity> findByChannelServiceAccess_IdAndGatewayChannel_Id(Long channelServiceAccessId, @Size(max = 36) String gatewayChannelId);

    List<ChannelServiceDefinitionEntity> findByGatewayChannel_Id(@Size(max = 36) String gatewayChannelId);

    @EntityGraph(attributePaths = {"channelServiceAccess", "channelServiceAccess.service", "gatewayChannel", "definition"})
    List<ChannelServiceDefinitionEntity> findByType(ChannelServiceDefinitionType type);

    @EntityGraph(attributePaths = {"channelServiceAccess", "channelServiceAccess.service", "gatewayChannel", "definition"})
    List<ChannelServiceDefinitionEntity> findByTypeAndGatewayChannel_NameIn(ChannelServiceDefinitionType type,
                                                                            List<String> gatewayNames);

    Page<ChannelServiceDefinitionEntity> findAllByTypeIn(List<ChannelServiceDefinitionType> types, Pageable pageable);

    List<ChannelServiceDefinitionEntity> findByChannelServiceAccess_Id(Long channelServiceAccessId);
}
