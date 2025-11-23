package ir.daneshrefah.scm.common.dto.plugin;

import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PluginBindingRequest {
    private String pluginId;
    private String definitionId;
    private PluginScope scope;
    private String scopeId;
}
