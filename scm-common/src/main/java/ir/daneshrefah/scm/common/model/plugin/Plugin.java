package ir.daneshrefah.scm.common.model.plugin;

import ir.daneshrefah.scm.common.AbstractAuditableModel;
import ir.daneshrefah.scm.common.model.definition.Definition;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link ir.daneshrefah.scm.core.entity.plugin.PluginEntity}
 */
@Getter
@Setter
public class Plugin extends AbstractAuditableModel<String> {
    @Size(max = 36)
    private String id;

    @Size(max = 50)
    private String name;

    @Size(max = 100)
    private String title;

    private PluginType type;

    @Size(max = 255)
    private String description;

    private Definition definition;
}