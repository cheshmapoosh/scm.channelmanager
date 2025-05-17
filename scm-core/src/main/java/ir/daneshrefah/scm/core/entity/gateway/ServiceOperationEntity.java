package ir.daneshrefah.scm.core.entity.gateway;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.core.entity.definition.DefinitionEntity;
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
@Table(name = "TBL_SCM_SERVICE_OPERATION", schema = "REF",
        uniqueConstraints = {
                @UniqueConstraint(name = "UC_GTW_OPT_ON_SVC_OPT", columnNames = {"EB_SERVICE_ID", "OPERATION_NAME"})
        })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class ServiceOperationEntity extends AbstractAuditableEntity<String> {
    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "SERVICE_OPERATION_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @NotNull
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "EB_SERVICE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_GTW_OPT_ON_SVC"))
    private ServiceEntity service;

    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String operationName;

    @ManyToOne
    @JoinColumn(name = "DEFINITION_ID", foreignKey = @ForeignKey(name = "FK_GTW_OPT_ON_DEF"))
    private DefinitionEntity definition;

}
