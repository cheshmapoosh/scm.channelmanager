package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.dto.asset.ChannelServiceAccess;
import ir.daneshrefah.scm.common.model.definition.Definition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity}
 */
@Getter
@Setter
public class ChannelServiceDefinition extends AbstractAuditableModel<String> {
    @Size(max = 36)
    private String id;
    @NotNull
    private ChannelServiceAccess channelServiceAccess;
    private GatewayChannel gatewayChannel;
    private ChannelServiceDefinitionType type;
    private Definition definition;
}