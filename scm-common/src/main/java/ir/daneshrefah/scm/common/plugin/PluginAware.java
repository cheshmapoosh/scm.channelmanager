package ir.daneshrefah.scm.common.plugin;

import org.apache.camel.Exchange;

public interface PluginAware {

    void supports(Exchange exchange) throws Exception;

    void before(Exchange exchange) throws Exception;

    void after(Exchange exchange) throws Exception;

    void afterThrowing(Exchange exchange) throws Exception;
}
