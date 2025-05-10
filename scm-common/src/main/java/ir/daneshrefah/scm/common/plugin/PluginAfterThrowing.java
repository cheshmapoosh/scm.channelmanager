package ir.daneshrefah.scm.common.plugin;

import org.apache.camel.Exchange;

public interface PluginAfterThrowing {
    void afterThrowing(Exchange exchange) throws Exception;
}
