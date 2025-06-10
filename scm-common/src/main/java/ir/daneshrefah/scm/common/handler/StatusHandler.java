package ir.daneshrefah.scm.common.handler;

import org.apache.camel.Exchange;

public interface StatusHandler {
    void handle(Exchange exchange);
}
