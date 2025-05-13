package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.protocol.ProtocolType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "TBL_SCM_GATEWAY_CHANNEL",
        schema = "REF",
        uniqueConstraints = {
                @UniqueConstraint(name = "UC_GTW_CHN_ON_PTC_CHN", columnNames = {"CHANNEL_ID", "PROTOCOL_TYPE"}),
                @UniqueConstraint(name = "UC_GTW_CHN_ON_PTC_CHN", columnNames = {"CODE"})
        })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class GatewayChannelEntity extends AbstractAuditableEntity<String> {
    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "GATEWAY_CHANNEL_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 50)
    @Column(nullable = false, length = 50, unique = true)
    private String code;

    @NotNull
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Boolean active = false;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @ManyToOne
    @JoinColumn(name = "CHANNEL_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_GTW_CHN_ON_CHN"))
    private ChannelEntity channel;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ProtocolType protocolType;

    @Size(max = 50)
    @Column(length = 50)
    private String host;

    private Short port;

    @Size(max = 100)
    @Column(length = 100)
    private String path;

    @Size(max = 2048)
    @Column(length = 2048)
    private String metadata;

}
