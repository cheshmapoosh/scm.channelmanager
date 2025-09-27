package ir.daneshrefah.scm.common.dto.definition;

import ir.daneshrefah.scm.common.model.definition.DefinitionDetail;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DefinitionDetailResponse {
    private String id;
    private List<? extends DefinitionDetail> detail;
}
