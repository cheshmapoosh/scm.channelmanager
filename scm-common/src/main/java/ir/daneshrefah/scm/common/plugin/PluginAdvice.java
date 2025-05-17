package ir.daneshrefah.scm.common.plugin;

import ir.daneshrefah.scm.common.model.plugin.PluginPhase;
import org.apache.camel.Exchange;

import java.util.Map;

public interface PluginAdvice {

    boolean supports(Exchange exchange, PluginPhase phase, Map<String, ?> config) throws Exception;

    void before(Exchange exchange, Map<String, ?> config) throws Exception;

    void after(Exchange exchange, Map<String, ?> config) throws Exception;

    void afterThrowing(Exchange exchange, Map<String, ?> config) throws Exception;
}
