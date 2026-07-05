package ir.daneshrefah.scm.common.event.plugin;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmPluginEventType implements ScmEventType {
    PLUGIN_BEFORE_STARTED("plugin.before.started"),
    PLUGIN_BEFORE_COMPLETED("plugin.before.completed"),
    PLUGIN_AFTER_STARTED("plugin.after.started"),
    PLUGIN_AFTER_COMPLETED("plugin.after.completed"),
    PLUGIN_FAILED("plugin.failed");

    private final String code;

    ScmPluginEventType(String code) {
        this.code = code;
    }

    @Override
    public String code() {
        return code;
    }
}
