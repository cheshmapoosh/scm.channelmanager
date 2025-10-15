package ir.daneshrefah.scm.common.dto.plugin;

import ir.daneshrefah.scm.common.dto.definition.DefinitionResponse;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PluginBindingSearchResponse {
    private String id;
    private PluginScope scope;
    private String scopeId;
    private Boolean active;
    private DefinitionResponse definition;
}
