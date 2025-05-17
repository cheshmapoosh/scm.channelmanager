package ir.daneshrefah.scm.core.entity.definition;

import ir.daneshrefah.scm.common.data.entity.AbstractAuditableEntity;
import ir.daneshrefah.scm.common.model.template.TemplateEngine;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Table(name = "TBL_SCM_DEFINITION", schema = "REF",
uniqueConstraints = {@UniqueConstraint(name = "UC_DEF_ON_NAME", columnNames = "NAME")})
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionEntity extends AbstractAuditableEntity<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "DEFINITION_ID")
    private String id;
    @Size(max = 100)
    @Column(length = 100, nullable = false)
    private String title;
    @Size(max = 50)
    @Column(length = 50, nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Size(max = 20)
    @Column(length = 20)
    private TemplateEngine engine;
    @Size(max = 2048)
    private String details;
}
