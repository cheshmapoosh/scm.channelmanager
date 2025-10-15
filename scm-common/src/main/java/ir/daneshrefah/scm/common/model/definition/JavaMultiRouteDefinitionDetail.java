package ir.daneshrefah.scm.common.model.definition;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JavaMultiRouteDefinitionDetail extends DefinitionDetail {
    private String contextPath;
    private List<MultiRouteDetail> multiRouteDetails;
}
