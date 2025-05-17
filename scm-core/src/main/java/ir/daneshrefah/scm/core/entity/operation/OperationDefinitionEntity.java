package ir.daneshrefah.scm.core.entity.operation;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.operation.OperationDefinitionType;
import ir.daneshrefah.scm.core.entity.definition.DefinitionEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "TBL_SCM_OPERATION_DEFINITION", schema = "REF",
uniqueConstraints = {@UniqueConstraint(name = "UC_OPT_DEF_ON_OPT_DEF_TYP", columnNames = {"OPERATION_ID, DEFINITION_ID, TYPE"})})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OperationDefinitionEntity extends AbstractAuditableEntity<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OPERATION_DEFINITION_ID")
    private String id;

    @Enumerated(EnumType.STRING)
    @Size(max = 20)
    @Column(length = 20, nullable = false)
    private OperationDefinitionType type;

    @ManyToOne
    @JoinColumn(name = "OPERATION_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_OPT_PVD_ON_OPT"))
    private OperationEntity operation;

    @ManyToOne
    @JoinColumn(name = "DEFINITION_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_OPT_PVD_ON_DEF"))
    private DefinitionEntity definition;
}
