package ir.daneshrefah.scm.common.event.plugin;

import ir.daneshrefah.scm.common.event.ScmEventType;

public enum ScmPluginEventType implements ScmEventType {
    PLUGIN_BEFORE_STARTED,
    PLUGIN_BEFORE_COMPLETED,
    PLUGIN_AFTER_STARTED,
    PLUGIN_AFTER_COMPLETED,
    PLUGIN_FAILED;

    @Override
    public String code() {
        return name();
    }
}
