package ir.daneshrefah.scm.common.model.plugin;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.definition.Definition;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO for {@link PluginBindingEntity}
 */
@Getter
@Setter
public class PluginBinding extends AbstractAuditableModel<String> {
    @Size(max = 36)
    private String id;
    private PluginScope scope;
    @Size(max = 36)
    private String scopeId;
    @NotNull
    private Boolean active;
    private Definition definition;
    List<PluginDetail> details;
}