package ir.daneshrefah.scm.common.data.audit.model;

import ir.daneshrefah.scm.common.data.audit.model.constants.PrimaryKeyType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class AuditEntityMetaData {
    private String auditEntity;
    private PrimaryKeyType primaryKeyType;
}
