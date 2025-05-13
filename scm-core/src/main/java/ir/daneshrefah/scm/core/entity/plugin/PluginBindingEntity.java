package ir.daneshrefah.scm.core.entity.plugin;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import ir.daneshrefah.scm.core.entity.definition.DefinitionEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Setter
@Getter
@Entity
@Table(name = "TBL_SCM_PLUGIN_BINDING", schema = "REF",
        uniqueConstraints = {
                @UniqueConstraint(name = "UC_PLG_BND_ON_PLG_SCP", columnNames = {"PLUGIN_ID", "SCOPE", "SCOPE_ID"})
        }
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class PluginBindingEntity extends AbstractAuditableEntity<String> {
    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "PLUGIN_BINDING_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @ManyToOne
    @JoinColumn(nullable = false, name = "PLUGIN_ID", foreignKey = @ForeignKey(name = "FK_PLG_BND_ON_PLG"))
    private PluginEntity plugin;

    @Enumerated(EnumType.STRING)
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private PluginScope scope;

    @Size(max = 36)
    @Column(nullable = false, length = 36)
    private String scopeId;

    @Column(nullable = false)
    private Short order;

    @Enumerated(EnumType.STRING)
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private PluginPhase phase;

    @NotNull
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "DEFINITION_ID", foreignKey = @ForeignKey(name = "FK_PLG_ON_DEF"))
    private DefinitionEntity definition;
}
