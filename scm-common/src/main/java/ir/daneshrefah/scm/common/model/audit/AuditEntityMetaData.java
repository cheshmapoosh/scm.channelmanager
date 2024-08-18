package ir.daneshrefah.scm.common.model.audit;

import ir.daneshrefah.scm.common.model.audit.constants.PrimaryKeyType;
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
