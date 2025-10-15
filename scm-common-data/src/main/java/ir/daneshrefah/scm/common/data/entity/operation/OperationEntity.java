package ir.daneshrefah.scm.common.data.entity.operation;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.operation.OperationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "TBL_SCM_OPERATION", schema = "REF",
uniqueConstraints = {@UniqueConstraint(name = "UC_OPT_ON_NAME", columnNames = {"NAME"})})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class OperationEntity extends AbstractAuditableEntity<String> {
    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OPERATION_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @Size(max = 50)
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Size(max = 100)
    @Column(length = 100)
    private String path;

    @NotNull
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Boolean active = false;

    @Size(max = 255)
    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE",nullable = false, length = 20)
    private OperationType type;

    @ManyToOne
    @JoinColumn(name = "OPERATION_PROVIDER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_OPT_ON_OPT_PVD"))
    private OperationProviderEntity provider;

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OperationDefinitionEntity> definitions;
}
