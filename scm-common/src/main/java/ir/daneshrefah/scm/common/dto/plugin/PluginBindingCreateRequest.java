package ir.daneshrefah.scm.common.dto.plugin;

import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PluginBindingCreateRequest {
    @NotNull
    private PluginScope scope;
    @NotBlank
    private String scopeId;
    @NotNull
    private Boolean active;
    @NotBlank
    private String definitionId;
}
