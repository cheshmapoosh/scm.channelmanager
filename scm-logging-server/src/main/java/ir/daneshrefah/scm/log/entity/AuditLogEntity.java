package ir.daneshrefah.scm.log.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "SCM_AUDIT")
public class AuditLogEntity extends AbstractLogEntity {
    @Column(name = "CREATOR")
    private String CREATOR;
    @Column(name = "MODIFY_BY")
    private String modifyBy;
    @Column(name = "CLASS_TYPE")
    private String classType;
    @Column(name = "AUDIT_TIME")
    private Timestamp timestamp;
    @Column(name = "REVISION_TYPE")
    private String revisionType;
}
