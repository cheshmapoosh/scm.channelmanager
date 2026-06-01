package ir.daneshrefah.scm.core.integration.gateway.contract;

import ir.daneshrefah.scm.common.model.error.ScmFault;
import org.apache.camel.Exchange;

public interface FaultContractEncoder {
    Object encode(Exchange exchange, ScmFault fault, ClientContract contract);
}
