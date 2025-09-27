package ir.daneshrefah.scm.common.dto.definition;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.definition.DefinitionType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DefinitionFilterRequest extends PagedRequestData {
    private List<DefinitionType> types;
}
