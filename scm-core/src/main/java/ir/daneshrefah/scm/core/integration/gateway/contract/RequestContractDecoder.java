package ir.daneshrefah.scm.core.integration.gateway.contract;

import org.apache.camel.Exchange;

public interface RequestContractDecoder {
    void decode(Exchange exchange, ClientContract contract);
}
