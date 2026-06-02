package ir.daneshrefah.scm.core.integration.gateway.contract;

import org.apache.camel.Exchange;

public interface ResponseContractEncoder {
    Object encode(Exchange exchange, ClientContract contract);
}
