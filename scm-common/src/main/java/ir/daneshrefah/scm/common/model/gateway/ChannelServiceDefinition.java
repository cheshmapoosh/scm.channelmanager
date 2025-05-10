package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AuditableModel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.gateway.ChannelServiceDefinitionEntity}
 */
@Getter
@Setter
public class ChannelServiceDefinition extends AuditableModel<String> {
    @Size(max = 36)
    private String id;
    @NotNull
    private ChannelServiceAccess channelServiceAccess;
    @Size(max = 50)
    private String gatewayChannelCode;
    private ChannelServiceDefinitionType type;
    @Size(max = 2048)
    private String metadata;
}