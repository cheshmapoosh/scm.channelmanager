package ir.daneshrefah.scm.common.data.entity.operation;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "TBL_SCM_OPERATION_PROVIDER", schema = "REF",
        uniqueConstraints = {@UniqueConstraint(name = "UC_OPT_PVD_ON_CODE", columnNames = "NAME")}
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationProviderEntity extends AbstractAuditableEntity<String> {

    @Size(max = 36)
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OPERATION_PROVIDER_ID", nullable = false, updatable = false, unique = true, length = 36)
    private String id;

    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String title;

    @Size(max = 50)
    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String uri;

    @NotNull
    @Column(nullable = false)
    @JdbcTypeCode(SqlTypes.SMALLINT)
    private Boolean active = false;
}