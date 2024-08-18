package ir.daneshrefah.scm.common.data.audit.domain;

import ir.daneshrefah.scm.common.data.audit.converter.RevisionTypeConverter;
import ir.daneshrefah.scm.common.model.audit.constants.RevisionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "TBL_SCM_AUDIT_LOG")
@Setter
@Getter
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "REVISION_TYPE")
    @Convert(converter = RevisionTypeConverter.class)
    private RevisionType revisionType;
    private Timestamp timestamp;
    private String creator;
    private String modifyBy;
    @Column(name = "CLASS_TYPE")
    private String classType;
    @Column(name = "TYPE_ID")
    private String typeId;
    @Column(name = "VALUE",columnDefinition = "CLOB")
    private String value;
}
