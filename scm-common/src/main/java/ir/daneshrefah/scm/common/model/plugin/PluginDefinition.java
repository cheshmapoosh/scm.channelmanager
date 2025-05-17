package ir.daneshrefah.scm.common.model.plugin;

import lombok.Data;

import java.util.Map;

@Data
public class PluginDefinition {
    private String name;
    private Short order;
    private PluginPhase phase;
    private Map<String, ?> config;
}
