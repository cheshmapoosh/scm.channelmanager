package ir.daneshrefah.scm.core.entity.plugin;


import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.plugin.PluginType;
import ir.daneshrefah.scm.core.entity.definition.DefinitionEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "TBL_SCM_PLUGIN", schema = "REF",
        uniqueConstraints = @UniqueConstraint(name = "UC_PLG_ON_NAME", columnNames = "NAME")
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class PluginEntity extends AbstractAuditableEntity<String> {
    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "PLUGIN_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @Size(max = 100)
    @Column(length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private PluginType type;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @ManyToOne
    @JoinColumn(name = "DEFINITION_ID", foreignKey = @ForeignKey(name = "FK_PLG_ON_DEF"))
    private DefinitionEntity definition;
}
