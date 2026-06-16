package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.data.entity.asset.ChannelServiceAccessEntity;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import ir.daneshrefah.scm.common.data.entity.definition.DefinitionEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_CHN_SVC_DEFINITION", schema = "REF",
        uniqueConstraints = {
                @UniqueConstraint(name = "UC_GTW_ON_PTC_CHN", columnNames = {"CHANNEL_SERVICE_ACCESS_ID", "GATEWAY_CHANNEL_ID", "TYPE"})
        })
public class ChannelServiceDefinitionEntity extends AbstractAuditableEntity<String> {
    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CHN_SVC_DEFINITION_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "CHANNEL_SERVICE_ACCESS_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CHN_SVC_DEF_ON_CHN_SVC"))
    private ChannelServiceAccessEntity channelServiceAccess;

    @Size(max = 36)
    @ManyToOne(optional = false)
    @JoinColumn(name = "GATEWAY_CHANNEL_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CHN_SVC_DEF_ON_GTW_CHN"))
    private GatewayChannelEntity gatewayChannel;

    @Convert(converter = ChannelServiceDefinitionTypeConverter.class)
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private ChannelServiceDefinitionType type;

    @ManyToOne
    @JoinColumn(name = "DEFINITION_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CHN_SVC_DEF_ON_DEF"))
    private DefinitionEntity definition;

}

