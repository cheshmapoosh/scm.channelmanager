package ir.daneshrefah.scm.core.entity.plugin;

import ir.daneshrefah.scm.common.model.definition.Definition;
import ir.daneshrefah.scm.common.model.plugin.PluginScope;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO for {@link PluginBindingEntity}
 */
@Getter
@Setter
public class PluginBindingEntityDto implements Serializable {
    @Size(max = 36)
    private String id;
    private PluginScope scope;
    @Size(max = 36)
    private String scopeId;
    @NotNull
    private Boolean active;
    private Definition definition;
}