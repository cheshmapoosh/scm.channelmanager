package ir.daneshrefah.scm.common.data.audit.model;

import ir.daneshrefah.scm.common.data.audit.model.constants.RevisionType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class AuditDetails {
    private Class<?> type;
    private MetaData metaData;
    private Object instanceId;
    private Object data;
    private RevisionType revisionType;


}
