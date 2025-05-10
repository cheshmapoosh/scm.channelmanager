package ir.daneshrefah.scm.common.plugin;

import org.apache.camel.Exchange;

public interface PluginAfter {
    void after(Exchange exchange) throws Exception;
}
