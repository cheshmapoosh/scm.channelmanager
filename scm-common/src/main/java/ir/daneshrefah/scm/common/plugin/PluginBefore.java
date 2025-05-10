package ir.daneshrefah.scm.common.plugin;

import org.apache.camel.Exchange;

public interface PluginBefore {
    void before(Exchange exchange) throws Exception;
}
