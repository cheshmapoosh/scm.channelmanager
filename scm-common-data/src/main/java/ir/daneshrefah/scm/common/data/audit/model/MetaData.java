package ir.daneshrefah.scm.common.data.audit.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Setter
@Getter
@Accessors(chain = true)
public class MetaData {
    private List<Parameter> parameters;
    private String idFieldName;
}
