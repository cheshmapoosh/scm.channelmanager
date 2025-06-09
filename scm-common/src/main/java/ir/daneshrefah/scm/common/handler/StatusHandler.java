package ir.daneshrefah.scm.common.handler;

import org.apache.camel.Exchange;

public interface StatusHandler {
    boolean isSuccess(Exchange exchange);
    void errorHandle(Exchange exchange);
}
