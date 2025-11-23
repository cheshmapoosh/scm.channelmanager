package ir.daneshrefah.scm.common.model.definition;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PluginDefinitionDetail extends DefinitionDetail {
    private String name;
    private Integer order;
    private String phase;
    private boolean active;
}
