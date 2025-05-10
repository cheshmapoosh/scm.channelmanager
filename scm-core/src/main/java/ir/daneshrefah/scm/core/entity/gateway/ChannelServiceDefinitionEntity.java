package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinitionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_CHANNEL_SERVICE_DEF", schema = "REF",
        uniqueConstraints = {
                @UniqueConstraint(name = "UC_GTW_ON_PTC_CHN", columnNames = {"CHANNEL_SERVICE_ACCESS_ID", "GATEWAY_CHANNEL_CODE", "TYPE"})
        })
public class ChannelServiceDefinitionEntity extends AbstractAuditableEntity<String> {
    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CHANNEL_SERVICE_DEF_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "CHANNEL_SERVICE_ACCESS_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_CHN_SVC_DEF_ON_CHN_SVC"))
    private ChannelServiceAccessEntity channelServiceAccess;

    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String gatewayChannelCode;

    @Enumerated(EnumType.STRING)
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private ChannelServiceDefinitionType type;

    @Size(max = 2048)
    @Column(length = 2048)
    private String metadata;


}

