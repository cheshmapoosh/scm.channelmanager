package ir.daneshrefah.scm.common.model.gateway;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.gateway.RouteChannelEntity}
 */
@Getter
@Setter
public class GatewayChannel extends AbstractAuditableModel<String> {
    @Size(max = 36)
    private String id;
    @Size(max = 100)
    private String name;
    @Size(max = 50)
    private String code;
    @NotNull
    private Boolean active = false;
    @Size(max = 100)
    private String title;
    @Size(max = 255)
    private String description;
    private Channel channel;
    private ProtocolType protocolType;
    @Size(max = 50)
    private String host;
    private Short port;
    @Size(max = 100)
    private String path;
    @Size(max = 2048)
    private String metadata;
}