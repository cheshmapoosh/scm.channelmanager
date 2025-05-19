package ir.daneshrefah.scm.core.integration.template.context;

import org.apache.camel.Exchange;

public interface ContextValueResolver {
    boolean supports(String key);
    Object resolve(String key, Exchange exchange);
}